package com.soukmar.app.ui.screens.deposerannonce

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soukmar.app.data.local.TokenManager
import com.soukmar.app.data.remote.dto.AttributeDefinitionDto
import com.soukmar.app.data.remote.dto.ListingDto
import com.soukmar.app.data.remote.dto.ListingUpsertRequest
import com.soukmar.app.data.remote.dto.SubcategoryWithAttributesDto
import com.soukmar.app.data.repository.ApiResult
import com.soukmar.app.data.repository.CatalogRepository
import com.soukmar.app.data.repository.ListingRepository
import com.soukmar.app.data.repository.UploadRepository
import com.soukmar.app.ui.model.CONDITION_CATEGORIES
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
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
    val currency: String = "MAD",
    val city: String = "",
    val phone: String = "",
    val whatsapp: String = "",
    val showPhone: Boolean = true,
    val attributes: Map<String, JsonElement> = emptyMap()
)

@HiltViewModel
class DeposerAnnonceViewModel @Inject constructor(
    private val listingRepository: ListingRepository,
    private val catalogRepository: CatalogRepository,
    private val uploadRepository: UploadRepository,
    private val tokenManager: TokenManager
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

    val maxPhotos: Int get() = if (isPremium) 20 else 10

    fun init(id: String?) {
        viewModelScope.launch { isLoggedIn = tokenManager.isLoggedIn() }
        if (id != null && editId == null) {
            editId = id
            loadForEdit(id)
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
        val attrs = listing.attributeValues.associate { av ->
            val code = av.attributeDefinition?.code ?: ""
            // Numbers are kept as string primitives (not JsonPrimitive(Number)) so
            // isString stays the TEXT/NUMBER/SELECT marker attrTextValue() relies on.
            val value: JsonElement = when {
                av.valueText != null -> JsonPrimitive(av.valueText)
                av.valueNumber != null -> JsonPrimitive(av.valueNumber.let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() })
                av.valueBoolean != null -> JsonPrimitive(av.valueBoolean)
                else -> JsonPrimitive("")
            }
            code to value
        }.filterKeys { it.isNotEmpty() }

        form = ListingFormState(
            category = listing.category,
            subcategoryId = listing.subcategoryId ?: "",
            condition = listing.condition ?: "",
            title = listing.title,
            description = listing.description,
            price = listing.price?.let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() } ?: "",
            currency = listing.currency,
            city = listing.city,
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

    val showCondition: Boolean get() = form.category.isNotEmpty() && CONDITION_CATEGORIES.contains(form.category)

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
                    val v = form.attributes[def.code] as? JsonPrimitive
                    v != null && v.content.isNotEmpty()
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
                        error = "Le téléchargement des photos a échoué."
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
                currency = form.currency,
                category = form.category,
                subcategoryId = form.subcategoryId.ifEmpty { null },
                condition = form.condition.ifEmpty { null },
                city = form.city,
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
                    error = "La publication a échoué. Réessayez."
                }
            }
        }
    }
}
