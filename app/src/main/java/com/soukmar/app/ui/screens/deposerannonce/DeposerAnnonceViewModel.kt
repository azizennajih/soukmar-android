package com.soukmar.app.ui.screens.deposerannonce

import com.soukmar.app.data.i18n.appText
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soukmar.app.data.country.CountryRepository
import com.soukmar.app.data.local.TokenManager
import com.soukmar.app.data.remote.dto.AttributeDefinitionDto
import com.soukmar.app.data.remote.dto.ListingDto
import com.soukmar.app.data.remote.dto.PlaceHitDto
import com.soukmar.app.data.remote.dto.ListingUpsertRequest
import com.soukmar.app.data.remote.dto.SubcategoryWithAttributesDto
import com.soukmar.app.data.repository.ApiResult
import com.soukmar.app.data.repository.CatalogRepository
import com.soukmar.app.data.repository.ListingRepository
import com.soukmar.app.data.repository.UploadRepository
import com.soukmar.app.data.repository.AiDraftResult
import com.soukmar.app.data.remote.dto.AiListingDraftDto
import com.soukmar.app.ui.model.CATEGORIES
import com.soukmar.app.ui.model.CITIES_BY_COUNTRY
import com.soukmar.app.ui.model.CONDITION_CATEGORIES
import com.soukmar.app.ui.model.MOROCCO_CITIES
import com.soukmar.app.ui.model.NO_CONDITION_SUBCATEGORIES
import com.soukmar.app.ui.model.currencyForCountry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import javax.inject.Inject

/** A photo either freshly picked from the gallery (only [localUri] set) or
 * already stored on the listing being edited (only [remoteUrl] set) — mirrors
 * the web's `PhotoItem { url, file? }`. Coil accepts either as its model. */
data class PhotoItem(val remoteUrl: String? = null, val localUri: Uri? = null) {
    val previewModel: Any get() = localUri ?: (remoteUrl ?: "")
}

val DEPOSER_STEPS = listOf("Catégorie", "Sous-catégorie", "Détails", "Photos", "Contact")

data class ListingFormState(
    val category: String = "",
    val subcategoryId: String = "",
    val condition: String = "",
    val title: String = "",
    val description: String = "",
    val price: String = "",
    val city: String = "",
    /** No manual currency field on purpose — mirrors web's deposer-annonce
     * (post-international-country-tranche): currency is always derived from
     * [country] via currencyForCountry(), never chosen independently. */
    val country: String = CountryRepository.DEFAULT_COUNTRY,
    val phone: String = "",
    val whatsapp: String = "",
    val showPhone: Boolean = true,
    val attributes: Map<String, JsonElement> = emptyMap()
) {
    val derivedCurrency: String get() = currencyForCountry(country)
    val citiesForCountry: List<String> get() = if (country == "MA") MOROCCO_CITIES else (CITIES_BY_COUNTRY[country] ?: emptyList())
}

/** Which question the AI assistant is waiting on. */
enum class AiConfirm { EDITED_PREVIEW, FORM_TEXTS }

@HiltViewModel
class DeposerAnnonceViewModel @Inject constructor(
    private val listingRepository: ListingRepository,
    private val catalogRepository: CatalogRepository,
    private val uploadRepository: UploadRepository,
    private val tokenManager: TokenManager,
    private val countryRepository: CountryRepository
) : ViewModel() {

    var isLoggedIn by mutableStateOf(false)
        private set

    var step by mutableStateOf(0)
        private set
    var loading by mutableStateOf(false)
        private set
    var uploading by mutableStateOf(false)
        private set
    var loadingSubcats by mutableStateOf(false)
        private set
    var success by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    var editId: String? = null
        private set
    var initLoading by mutableStateOf(false)
        private set
    val isEdit: Boolean get() = editId != null

    var subcategories by mutableStateOf<List<SubcategoryWithAttributesDto>>(emptyList())
        private set
    var attributeDefs by mutableStateOf<List<AttributeDefinitionDto>>(emptyList())
        private set

    var photos by mutableStateOf<List<PhotoItem>>(emptyList())
        private set
    var isPremium by mutableStateOf(false)
        private set

    var form by mutableStateOf(ListingFormState())
        private set

    var pendingCancel by mutableStateOf(false)
        private set

    private var countrySyncStarted = false

    val hasProgress: Boolean
        get() = form.category.isNotEmpty() || form.subcategoryId.isNotEmpty() || form.title.isNotBlank() ||
            form.description.isNotBlank() || form.price.isNotBlank() || form.city.isNotEmpty() || photos.isNotEmpty()

    /** Mirrors the web's cancel() — only asks for confirmation if the user
     * has actually entered something, so closing an empty/just-opened
     * wizard doesn't nag. */
    fun requestClose(): Boolean {
        if (hasProgress) { pendingCancel = true; return false }
        return true
    }

    fun dismissCancel() { pendingCancel = false }

    val maxPhotos: Int get() = if (isPremium) 20 else 10

    fun init(id: String?, presetCategory: String? = null) {
        viewModelScope.launch { isLoggedIn = tokenManager.isLoggedIn() }
        if (id != null && editId == null) {
            editId = id
            loadForEdit(id)
        } else if (id == null && !countrySyncStarted) {
            countrySyncStarted = true
            checkAiStatus()
            // Opened from a category's listing page: skip the category step (web: ?categorie=).
            if (presetCategory != null && form.category.isBlank() && CATEGORIES.any { it.value == presetCategory }) selectCategory(presetCategory)
            // Country is centrally controlled by the app-wide CountrySwitcher
            // (Home/Login/Listings), never a separate choice within this
            // form — kept in live sync with CountryRepository.country for as
            // long as the wizard is open (not just defaulted once), so
            // switching country elsewhere takes effect immediately. Mirrors
            // web's deposer-annonce.component.ts constructor effect().
            // snapshotFlow emits the current value immediately on collection,
            // so this also covers the initial default — no separate one-off
            // assignment needed. Editing an existing listing instead
            // preserves its own country, set in loadForEdit() — NOT
            // re-synced here.
            viewModelScope.launch {
                snapshotFlow { countryRepository.country }.collect { country ->
                    form = form.copy(country = country, city = "")
                }
            }
        }
    }

    private fun loadForEdit(id: String) {
        initLoading = true
        viewModelScope.launch {
            when (val result = listingRepository.getListing(id)) {
                is ApiResult.Success -> applyListingToForm(result.data)
                is ApiResult.Error -> error = result.message
            }
            initLoading = false
        }
    }

    private suspend fun applyListingToForm(listing: ListingDto) {
        // MULTI_SELECT produces one ListingAttributeValue row per selected
        // option (same attributeDefinitionId, several rows) — group by code
        // first so a naive one-row-per-code map doesn't silently drop all
        // but the last selected option when editing such a listing.
        val attrs = listing.attributeValues
            .filter { it.attributeDefinition?.code?.isNotEmpty() == true }
            .groupBy { it.attributeDefinition!!.code }
            .mapValues { (_, rows) ->
                if (rows.first().attributeDefinition!!.type == "MULTI_SELECT") {
                    JsonArray(rows.mapNotNull { it.valueText }.map { JsonPrimitive(it) })
                } else {
                    val av = rows.first()
                    // Numbers are kept as string primitives (not JsonPrimitive(Number)) so
                    // isString stays the TEXT/NUMBER/SELECT marker attrTextValue() relies on.
                    val value: JsonElement = when {
                        av.valueText != null -> JsonPrimitive(av.valueText)
                        av.valueNumber != null -> JsonPrimitive(av.valueNumber.let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() })
                        av.valueBoolean != null -> JsonPrimitive(av.valueBoolean)
                        else -> JsonPrimitive("")
                    }
                    value
                }
            }

        form = ListingFormState(
            category = listing.category,
            subcategoryId = listing.subcategoryId ?: "",
            condition = listing.condition ?: "",
            title = listing.title,
            description = listing.description,
            price = listing.price?.let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() } ?: "",
            city = listing.city,
            country = listing.country,
            phone = listing.phone ?: "",
            whatsapp = listing.whatsapp ?: "",
            showPhone = listing.showPhone != false,
            attributes = attrs
        )
        photos = listing.images.map { PhotoItem(remoteUrl = it) }

        if (listing.category.isNotEmpty()) {
            loadingSubcats = true
            when (val result = catalogRepository.getCategoryFull(listing.category)) {
                is ApiResult.Success -> {
                    subcategories = result.data.subcategories
                    attributeDefs = subcategories.find { it.id == listing.subcategoryId }?.attributeDefinitions ?: emptyList()
                }
                is ApiResult.Error -> { /* prefilled form still usable without the filter data */ }
            }
            loadingSubcats = false
        }
        step = 2
    }

    val showCondition: Boolean
        get() {
            if (form.category.isEmpty() || !CONDITION_CATEGORIES.contains(form.category)) return false
            val subCode = subcategories.find { it.id == form.subcategoryId }?.code ?: return true
            return subCode !in NO_CONDITION_SUBCATEGORIES
        }

    fun selectCategory(value: String) {
        form = form.copy(category = value, subcategoryId = "", attributes = emptyMap())
        attributeDefs = emptyList()
        loadingSubcats = true
        viewModelScope.launch {
            when (val result = catalogRepository.getCategoryFull(value)) {
                is ApiResult.Success -> {
                    subcategories = result.data.subcategories
                    step = if (subcategories.isNotEmpty()) 1 else 2
                }
                is ApiResult.Error -> {
                    subcategories = emptyList()
                    step = 2
                }
            }
            loadingSubcats = false
        }
    }

    fun selectSubcategory(sub: SubcategoryWithAttributesDto) {
        form = form.copy(subcategoryId = sub.id, attributes = emptyMap())
        attributeDefs = sub.attributeDefinitions
        step = 2
    }

    fun setAttrText(code: String, value: String) {
        form = form.copy(attributes = form.attributes + (code to JsonPrimitive(value)))
    }

    fun setAttrBool(code: String, value: Boolean) {
        form = form.copy(attributes = form.attributes + (code to JsonPrimitive(value)))
    }

    // setAttrText always stores a string primitive (isString = true) and
    // setAttrBool always stores an actual JSON boolean (isString = false),
    // so `isString` alone tells the two apart when reading a value back.
    fun attrTextValue(code: String): String = (form.attributes[code] as? JsonPrimitive)?.takeIf { it.isString }?.content ?: ""

    fun attrBoolValue(code: String): Boolean? {
        val prim = form.attributes[code] as? JsonPrimitive ?: return null
        return if (prim.isString) null else prim.boolean
    }

    fun attrMultiValue(code: String): List<String> {
        val arr = form.attributes[code] as? JsonArray ?: return emptyList()
        return arr.mapNotNull { (it as? JsonPrimitive)?.takeIf { p -> p.isString }?.content }
    }

    fun toggleAttrMulti(code: String, option: String) {
        val current = attrMultiValue(code)
        val updated = if (option in current) current - option else current + option
        form = form.copy(attributes = form.attributes + (code to JsonArray(updated.map { JsonPrimitive(it) })))
    }

    fun goBack() {
        step = if (step == 2 && subcategories.isEmpty()) 0 else step - 1
    }

    fun goNext() { step += 1 }

    val canNext: Boolean
        get() = when (step) {
            0 -> form.category.isNotEmpty()
            1 -> form.subcategoryId.isNotEmpty()
            2 -> form.title.isNotBlank() && form.description.isNotBlank() && form.city.isNotEmpty() &&
                attributeDefs.filter { it.required }.all { def ->
                    when (val v = form.attributes[def.code]) {
                        is JsonArray -> v.isNotEmpty()
                        is JsonPrimitive -> v.content.isNotEmpty()
                        else -> false
                    }
                }
            else -> true
        }

    fun addPhotos(uris: List<Uri>) {
        val room = maxPhotos - photos.size
        if (room <= 0) return
        photos = photos + uris.take(room).map { PhotoItem(localUri = it) }
    }

    fun removePhoto(index: Int) {
        photos = photos.toMutableList().apply { removeAt(index) }
    }

    /** Promotes a photo to the first slot (used as the listing's cover image)
     * — a simpler touch-friendly stand-in for the web's drag-to-reorder. */
    fun makePrimary(index: Int) {
        if (index <= 0 || index >= photos.size) return
        photos = photos.toMutableList().apply { add(0, removeAt(index)) }
    }

    fun updatePremium(value: Boolean) { isPremium = value }

    /** Towns and villages of the listing's country matching [query] (suggestions for the city fields). */
    suspend fun searchPlaces(query: String): List<PlaceHitDto> = listingRepository.searchPlaces(form.country, query)

    /** Start/destination listings (carpooling, transport…) call the first city the start city. */
    val hasDestinationCity: Boolean get() = attributeDefs.any { it.code == "DESTINATION_CITY" }

    /** A destination country is chosen separately for transport; then the destination city is free text. */
    val hasDestinationCountry: Boolean get() = attributeDefs.any { it.code == "DESTINATION_COUNTRY" }

    // ───────────── AI assistant ("create the listing with AI", mirrors the web component) ─────────────
    // State lives here, above the wizard steps, so photos, result and edited texts survive back/next.

    /** false until the server says an AI machine is set up: then the assistant is not shown at all. */
    var aiConfigured by mutableStateOf(false)
        private set
    /** null = not checked yet */
    var aiAvailable by mutableStateOf<Boolean?>(null)
        private set
    var aiMaxImages by mutableStateOf(6)
        private set
    var aiOpen by mutableStateOf(false)
        private set
    var aiLoading by mutableStateOf(false)
        private set
    var aiElapsed by mutableStateOf(0)
        private set
    var aiError by mutableStateOf<String?>(null)
        private set
    var aiNotice by mutableStateOf<String?>(null)
        private set
    var aiDraft by mutableStateOf<AiListingDraftDto?>(null)
        private set
    var aiTitle by mutableStateOf("")
    var aiDescription by mutableStateOf("")
    /** index into (main suggestion + alternatives) */
    var aiSelectedCategory by mutableStateOf(0)
    var aiConfirm by mutableStateOf<AiConfirm?>(null)
        private set

    /** photos the current draft was made from */
    private var aiSignature = ""
    private var aiPendingDraft: Pair<AiListingDraftDto, String>? = null
    /** Title/description as last written by the assistant: tells "still the AI text" from "typed by the user". */
    private var aiAppliedTitle = ""
    private var aiAppliedDescription = ""

    /** Only freshly picked photos can be analysed (they are still local files). */
    val aiUris: List<Uri> get() = photos.mapNotNull { it.localUri }.take(aiMaxImages)
    val aiTooManyPhotos: Boolean get() = photos.count { it.localUri != null } > aiMaxImages
    private fun aiSignatureNow(): String = aiUris.joinToString("|")
    val aiPhotosChanged: Boolean get() = aiDraft != null && aiSignatureNow() != aiSignature
    private val aiTitleEdited: Boolean get() = aiDraft?.let { aiTitle.trim() != it.suggestedTitle } ?: false
    private val aiDescriptionEdited: Boolean get() = aiDraft?.let { aiDescription.trim() != it.suggestedDescription } ?: false

    fun checkAiStatus() {
        viewModelScope.launch {
            val status = uploadRepository.aiStatus()
            aiConfigured = status?.configured == true
            aiAvailable = status?.available == true
            aiMaxImages = status?.maxImages?.takeIf { it > 0 } ?: 6
        }
    }

    fun toggleAi() {
        aiOpen = !aiOpen
        aiNotice = null
        if (aiOpen && aiAvailable == false) checkAiStatus()
    }

    fun aiAnalyse(force: Boolean = false) {
        val uris = aiUris
        if (uris.isEmpty() || aiLoading) return
        val signature = aiSignatureNow()
        aiLoading = true
        aiError = null
        aiNotice = null
        aiElapsed = 0
        val timer = viewModelScope.launch { while (true) { kotlinx.coroutines.delay(1000); aiElapsed++ } }
        viewModelScope.launch {
            val result = uploadRepository.aiListingDraft(uris, force)
            timer.cancel()
            aiLoading = false
            when (result) {
                is AiDraftResult.Success -> {
                    aiAvailable = true
                    // A new result never silently replaces texts the user edited in the preview.
                    if (aiTitleEdited || aiDescriptionEdited) {
                        aiPendingDraft = result.draft to signature
                        aiConfirm = AiConfirm.EDITED_PREVIEW
                    } else takeAiDraft(result.draft, signature, replaceTexts = true)
                }
                is AiDraftResult.Failed -> {
                    if (result.unavailable) aiAvailable = false
                    aiError = result.message ?: appText(if (result.unavailable) "ai.unavailable" else "ai.error_generic", "L'analyse des photos a échoué. Réessayez ou continuez manuellement.")
                }
            }
        }
    }

    private fun takeAiDraft(draft: AiListingDraftDto, signature: String, replaceTexts: Boolean) {
        val keepTitle = if (!replaceTexts && aiTitleEdited) aiTitle else null
        val keepDescription = if (!replaceTexts && aiDescriptionEdited) aiDescription else null
        aiDraft = draft
        aiSignature = signature
        aiSelectedCategory = 0
        aiTitle = keepTitle ?: draft.suggestedTitle
        aiDescription = keepDescription ?: draft.suggestedDescription
    }

    /** "Apply suggestion": texts the user wrote in the form are only replaced after a confirmation. */
    fun aiApply() {
        val title = aiTitle.trim()
        val description = aiDescription.trim()
        if (aiDraft == null || title.isEmpty() || description.isEmpty()) return
        val titleIsMine = form.title.isNotBlank() && form.title != aiAppliedTitle && form.title != title
        val descIsMine = form.description.isNotBlank() && form.description != aiAppliedDescription && form.description != description
        if (titleIsMine || descIsMine) aiConfirm = AiConfirm.FORM_TEXTS else applyAiDraft(replaceTexts = true)
    }

    fun aiResolveConfirm(replace: Boolean) {
        val kind = aiConfirm ?: return
        aiConfirm = null
        when (kind) {
            AiConfirm.EDITED_PREVIEW -> aiPendingDraft?.let { (draft, signature) -> takeAiDraft(draft, signature, replaceTexts = replace) }
            AiConfirm.FORM_TEXTS -> applyAiDraft(replaceTexts = replace)
        }
        aiPendingDraft = null
    }

    private fun applyAiDraft(replaceTexts: Boolean) {
        val draft = aiDraft ?: return
        val title = aiTitle.trim()
        val description = aiDescription.trim()
        val titleIsMine = form.title.isNotBlank() && form.title != aiAppliedTitle && form.title != title
        val descIsMine = form.description.isNotBlank() && form.description != aiAppliedDescription && form.description != description
        if (replaceTexts || !titleIsMine) { form = form.copy(title = title); aiAppliedTitle = title }
        if (replaceTexts || !descIsMine) { form = form.copy(description = description); aiAppliedDescription = description }

        val main = aiSelectedCategory == 0
        val choice = (listOf(draft.suggestedCategory) + draft.alternativeCategories).getOrNull(aiSelectedCategory) ?: draft.suggestedCategory
        // Attributes belong to the main suggestion's subcategory; they are dropped if the user picked an alternative.
        val suggested = if (main) draft.attributes else emptyMap()
        aiOpen = false
        viewModelScope.launch {
            val sameSub = form.category == choice.category && form.subcategoryId == (choice.subcategoryId ?: "")
            if (!sameSub) {
                form = form.copy(category = choice.category, subcategoryId = "", attributes = emptyMap())
                attributeDefs = emptyList()
                loadingSubcats = true
                subcategories = when (val result = catalogRepository.getCategoryFull(choice.category)) {
                    is ApiResult.Success -> result.data.subcategories
                    is ApiResult.Error -> emptyList()
                }
                loadingSubcats = false
                subcategories.find { it.id == choice.subcategoryId }?.let { sub ->
                    form = form.copy(subcategoryId = sub.id)
                    attributeDefs = sub.attributeDefinitions
                }
                if (!showCondition) form = form.copy(condition = "")
            }
            // Only attributes this subcategory really has, and never over a value the user already entered.
            var attrs = form.attributes
            for (def in attributeDefs) {
                val value = suggested[def.code] ?: continue
                if (attrs[def.code] == null) attrs = attrs + (def.code to value)
            }
            form = form.copy(attributes = attrs)
            // Subcategory still open (the AI only knew the category): let the user choose it, otherwise go to the details.
            step = if (subcategories.isNotEmpty() && form.subcategoryId.isEmpty()) 1 else 2
            aiNotice = if (replaceTexts) appText("ai.applied", "Proposition reprise.") else appText("ai.applied_kept_texts", "Vos textes ont été conservés.")
        }
    }

    fun updateForm(transform: (ListingFormState) -> ListingFormState) {
        form = transform(form)
    }

    fun publish(onDone: (String) -> Unit) {
        if (loading) return
        loading = true
        error = null
        viewModelScope.launch {
            val localUris = photos.mapNotNull { it.localUri }
            var uploadedUrls: List<String> = emptyList()
            if (localUris.isNotEmpty()) {
                uploading = true
                when (val result = uploadRepository.uploadImages(localUris)) {
                    is ApiResult.Success -> uploadedUrls = result.data
                    is ApiResult.Error -> {
                        uploading = false
                        loading = false
                        error = appText("deposer.error_upload", "Le téléchargement des photos a échoué.")
                        return@launch
                    }
                }
                uploading = false
            }
            var uploadIdx = 0
            val images = photos.map { it.localUri?.let { _ -> uploadedUrls[uploadIdx++] } ?: it.remoteUrl!! }

            val body = ListingUpsertRequest(
                title = form.title,
                description = form.description,
                price = form.price.toDoubleOrNull(),
                category = form.category,
                subcategoryId = form.subcategoryId.ifEmpty { null },
                condition = form.condition.ifEmpty { null },
                city = form.city,
                country = form.country,
                images = images,
                phone = form.phone,
                whatsapp = form.whatsapp,
                showPhone = form.showPhone,
                attributes = form.attributes
            )

            val result = editId?.let { listingRepository.updateListing(it, body) } ?: listingRepository.createListing(body)
            when (result) {
                is ApiResult.Success -> {
                    success = true
                    loading = false
                    onDone(result.data.id)
                }
                is ApiResult.Error -> {
                    loading = false
                    error = appText("deposer.error_publish", "La publication a échoué. Réessayez.")
                }
            }
        }
    }
}
