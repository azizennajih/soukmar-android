@file:OptIn(ExperimentalMaterial3Api::class)

package com.soukmar.app.ui.screens.deposerannonce

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.soukmar.app.data.remote.dto.AttributeDefinitionDto
import com.soukmar.app.data.remote.dto.SubcategoryWithAttributesDto
import com.soukmar.app.ui.components.ErrorBanner
import com.soukmar.app.ui.components.PrimaryButton
import com.soukmar.app.ui.components.TextAutocompleteField
import com.soukmar.app.ui.i18n.LocalI18n
import com.soukmar.app.ui.i18n.t
import com.soukmar.app.ui.i18n.tCatalog
import com.soukmar.app.ui.model.CATEGORIES
import com.soukmar.app.ui.model.COUNTRIES
import com.soukmar.app.ui.model.COUNTRY_REGIONS
import com.soukmar.app.ui.model.CategoryIcon
import com.soukmar.app.ui.model.JOB_PROFESSIONS_BY_SECTOR
import com.soukmar.app.ui.model.JOB_PROFESSION_CODES
import com.soukmar.app.ui.model.categoryConfig
import com.soukmar.app.ui.model.countryFlag
import com.soukmar.app.ui.model.localeForLang
import com.soukmar.app.ui.model.regionLabelKey
import java.util.Locale
import com.soukmar.app.ui.theme.BorderColor
import com.soukmar.app.ui.theme.Gold
import com.soukmar.app.ui.theme.GoldLight
import com.soukmar.app.ui.theme.Primary
import com.soukmar.app.ui.theme.PrimaryLight
import com.soukmar.app.ui.theme.TextMuted
import com.soukmar.app.ui.theme.TextPrimary
import com.soukmar.app.ui.theme.WhiteColor

@Composable
fun DeposerAnnonceScreen(
    editId: String?,
    onBack: () -> Unit,
    onRequireLogin: () -> Unit,
    onPublished: (String) -> Unit,
    viewModel: DeposerAnnonceViewModel = hiltViewModel()
) {
    LaunchedEffect(editId) { viewModel.init(editId) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isNotEmpty()) viewModel.addPhotos(uris)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isEdit) t("deposer.header_title_edit") else t("deposer.header_title")) },
                navigationIcon = {
                    IconButton(onClick = { if (viewModel.requestClose()) onBack() }) {
                        Icon(Icons.Filled.Close, contentDescription = t("common.cancel"))
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                !viewModel.isLoggedIn -> LoginGate(onRequireLogin)
                viewModel.success -> PublishSuccess(isEdit = viewModel.isEdit)
                viewModel.initLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
                else -> DeposerAnnonceContent(viewModel, onPickPhotos = { photoPicker.launch("image/*") }, onPublished = onPublished)
            }
        }
    }

    if (viewModel.pendingCancel) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissCancel() },
            text = { Text(t("deposer.confirm_cancel")) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissCancel(); onBack() }) {
                    Text(t("common.yes"), color = com.soukmar.app.ui.theme.ErrorColor)
                }
            },
            dismissButton = { TextButton(onClick = { viewModel.dismissCancel() }) { Text(t("common.no")) } }
        )
    }
}

@Composable
private fun LoginGate(onRequireLogin: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🔐", fontSize = 40.sp)
        Spacer(Modifier.height(12.dp))
        Text(t("deposer.gate_title"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        Text(t("deposer.gate_sub"), color = TextMuted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        PrimaryButton(text = t("deposer.gate_btn"), onClick = onRequireLogin, modifier = Modifier.fillMaxWidth(0.7f))
    }
}

@Composable
private fun PublishSuccess(isEdit: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("✅", fontSize = 40.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            if (isEdit) t("deposer.success_title_edit") else t("deposer.success_title"),
            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary
        )
        Spacer(Modifier.height(8.dp))
        Text(if (isEdit) t("deposer.success_sub_edit") else t("deposer.success_sub"), color = TextMuted)
    }
}

@Composable
private fun DeposerAnnonceContent(
    viewModel: DeposerAnnonceViewModel,
    onPickPhotos: () -> Unit,
    onPublished: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Stepper(step = viewModel.step, modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            when (viewModel.step) {
                0 -> CategoryStep(viewModel)
                1 -> SubcategoryStep(viewModel)
                2 -> DetailsStep(viewModel)
                3 -> PhotosStep(viewModel, onPickPhotos)
                4 -> ContactStep(viewModel)
            }
            viewModel.error?.let {
                Spacer(Modifier.height(12.dp))
                ErrorBanner(it)
            }
            Spacer(Modifier.height(16.dp))
        }

        HorizontalDivider(color = BorderColor)
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (viewModel.step > 0) {
                OutlinedButton(onClick = { viewModel.goBack() }, enabled = !viewModel.loading) { Text(t("deposer.back")) }
            } else {
                Spacer(Modifier.width(1.dp))
            }
            if (viewModel.step < DEPOSER_STEPS.size - 1) {
                Button(
                    onClick = { viewModel.goNext() },
                    enabled = viewModel.canNext,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.White)
                ) { Text(t("deposer.next")) }
            } else {
                Button(
                    onClick = { viewModel.publish(onPublished) },
                    enabled = viewModel.canNext && !viewModel.loading,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.White)
                ) {
                    if (viewModel.uploading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp)); Text(t("deposer.uploading"))
                    } else if (viewModel.loading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp)); Text(t("deposer.publishing"))
                    } else {
                        Text(if (viewModel.isEdit) t("deposer.publish_edit") else t("deposer.publish"))
                    }
                }
            }
        }
    }
}

private val DEPOSER_STEP_KEYS = listOf(
    "deposer.step_category",
    "deposer.step_subcategory",
    "deposer.step_details",
    "deposer.step_photos",
    "deposer.step_contact"
)

@Composable
private fun Stepper(step: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier.horizontalScroll(rememberScrollState()), verticalAlignment = Alignment.CenterVertically) {
        DEPOSER_STEPS.forEachIndexed { index, _ ->
            val done = index < step
            val active = index == step
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(if (done || active) Primary else BorderColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (done) "✓" else "${index + 1}",
                    color = if (done || active) Color.White else TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(6.dp))
            Text(t(DEPOSER_STEP_KEYS[index]), fontSize = 12.sp, color = if (active) TextPrimary else TextMuted, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal)
            if (index < DEPOSER_STEPS.size - 1) {
                Box(modifier = Modifier.width(16.dp).height(1.dp).background(if (done) Primary else BorderColor))
            }
        }
    }
}

@Composable
private fun CategoryStep(viewModel: DeposerAnnonceViewModel) {
    Text(t("deposer.cat_title"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
    Spacer(Modifier.height(12.dp))
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.heightIn(max = 2000.dp)
    ) {
        items(CATEGORIES, key = { it.value }) { cat ->
            val selected = viewModel.form.category == cat.value
            Column(
                modifier = Modifier
                    .clickable { viewModel.selectCategory(cat.value) }
                    .background(if (selected) PrimaryLight else cat.bg, RoundedCornerShape(14.dp))
                    .border(if (selected) 2.dp else 0.dp, Primary, RoundedCornerShape(14.dp))
                    .padding(vertical = 16.dp, horizontal = 8.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CategoryIcon(cat.value, tint = if (selected) Primary else cat.fg, modifier = Modifier.size(26.dp))
                Spacer(Modifier.height(6.dp))
                Text(tCatalog("cats.${cat.value}", cat.value), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (selected) Primary else cat.fg, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }
}

@Composable
private fun SubcategoryStep(viewModel: DeposerAnnonceViewModel) {
    Text(t("deposer.subcat_title"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
    Spacer(Modifier.height(12.dp))
    if (viewModel.loadingSubcats) {
        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.heightIn(max = 2000.dp)
    ) {
        items(viewModel.subcategories, key = { it.id }) { sub: SubcategoryWithAttributesDto ->
            val selected = viewModel.form.subcategoryId == sub.id
            Box(
                modifier = Modifier
                    .clickable { viewModel.selectSubcategory(sub) }
                    .background(if (selected) PrimaryLight else WhiteColor, RoundedCornerShape(12.dp))
                    .border(1.dp, if (selected) Primary else BorderColor, RoundedCornerShape(12.dp))
                    .padding(vertical = 14.dp, horizontal = 10.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(tCatalog("subcats.${sub.code}", sub.code), fontSize = 13.sp, color = if (selected) Primary else TextPrimary, fontWeight = FontWeight.Medium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }
}

@Composable
private fun DetailsStep(viewModel: DeposerAnnonceViewModel) {
    val form = viewModel.form
    Text(t("deposer.details_title"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
    Spacer(Modifier.height(12.dp))

    OutlinedTextField(
        value = form.title,
        onValueChange = { if (it.length <= 100) viewModel.updateForm { f -> f.copy(title = it) } },
        label = { Text(t("deposer.label_title")) },
        placeholder = { Text(t("deposer.placeholder_title_${form.category.lowercase()}")) },
        supportingText = { Text("${form.title.length}/100 ${t("deposer.chars")}") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, cursorColor = Primary)
    )
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(
        value = form.description,
        onValueChange = { viewModel.updateForm { f -> f.copy(description = it) } },
        label = { Text(t("deposer.label_desc")) },
        placeholder = { Text(t("deposer.placeholder_desc_${form.category.lowercase()}")) },
        minLines = 4,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, cursorColor = Primary)
    )
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(
        value = form.price,
        onValueChange = { if (it.all { c -> c.isDigit() }) viewModel.updateForm { f -> f.copy(price = it) } },
        label = { Text("${t("deposer.label_price")} (${form.derivedCurrency})") },
        placeholder = { Text("0") },
        singleLine = true,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, cursorColor = Primary)
    )
    Spacer(Modifier.height(12.dp))
    // Unconditional (all categories) country field, replacing the old fixed
    // MAD/EUR/USD currency picker — currency is now always derived from the
    // chosen country (form.derivedCurrency above), never chosen manually.
    // Mirrors web's deposer-annonce.component.html post-international-tranche.
    CountryDropdown(selected = form.country, onSelect = { viewModel.selectCountry(it) })
    Spacer(Modifier.height(12.dp))
    CityDropdown(selected = form.city, cities = form.citiesForCountry, onSelect = { viewModel.updateForm { f -> f.copy(city = it) } })

    if (viewModel.showCondition) {
        Spacer(Modifier.height(12.dp))
        Text(t("deposer.label_condition"), fontSize = 13.sp, color = TextMuted)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("NEW" to "Neuf", "USED" to "Occasion").forEach { (value, label) ->
                val selected = form.condition == value
                Box(
                    modifier = Modifier
                        .clickable { viewModel.updateForm { f -> f.copy(condition = value) } }
                        .background(if (selected) PrimaryLight else WhiteColor, RoundedCornerShape(999.dp))
                        .border(1.dp, if (selected) Primary else BorderColor, RoundedCornerShape(999.dp))
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                ) {
                    Text(label, color = if (selected) Primary else TextPrimary, fontSize = 13.sp)
                }
            }
        }
    }

    if (viewModel.attributeDefs.isNotEmpty()) {
        Spacer(Modifier.height(18.dp))
        Text("Caractéristiques", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        viewModel.attributeDefs.sortedBy { it.sortOrder }.forEach { def ->
            AttributeField(def, viewModel)
            Spacer(Modifier.height(12.dp))
        }
    }
}

/** [cities] empty means the chosen country has no curated list (most of the
 * ~195 countries don't) — falls back to a plain free-text field, exactly
 * like web's CountrySelectComponent/CityDropdown fallback (Listing.city is a
 * free string backend-side either way, only without suggestions). */
@Composable
private fun CityDropdown(selected: String, cities: List<String>, onSelect: (String) -> Unit) {
    if (cities.isEmpty()) {
        OutlinedTextField(
            value = selected,
            onValueChange = onSelect,
            label = { Text(t("deposer.label_city")) },
            placeholder = { Text(t("deposer.city_default")) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, cursorColor = Primary)
        )
        return
    }
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(t("deposer.label_city")) },
            placeholder = { Text(t("deposer.city_default")) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, cursorColor = Primary)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.heightIn(max = 320.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Rechercher…") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, cursorColor = Primary)
            )
            cities.filter { it.contains(query, ignoreCase = true) }.take(60).forEach { city ->
                DropdownMenuItem(text = { Text(city) }, onClick = { onSelect(city); expanded = false; query = "" })
            }
        }
    }
}

/** Searchable country picker grouped by continent (Morocco pinned first) —
 * mirrors web's CountrySelectComponent. Flag is a plain computed emoji
 * (unlike web's bundled-SVG FlagIconComponent workaround): Android renders
 * regional-indicator flag emoji natively via Noto Color Emoji with no known
 * issue, so there's no Windows-Chrome-style rendering bug to work around here. */
@Composable
private fun CountryDropdown(selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val uiLocale = localeForLang(LocalI18n.current.currentLang)
    fun displayName(code: String) = Locale("", code).getDisplayCountry(uiLocale).ifEmpty { code }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = "${countryFlag(selected)} ${displayName(selected)}",
            onValueChange = {},
            readOnly = true,
            label = { Text(t("deposer.label_country")) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, cursorColor = Primary)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.heightIn(max = 400.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Rechercher…") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, cursorColor = Primary)
            )
            val q = query.trim()
            fun pick(code: String) { onSelect(code); expanded = false; query = "" }
            if (q.isEmpty()) {
                COUNTRY_REGIONS.forEach { (region, codes) ->
                    if (codes.isEmpty()) return@forEach
                    Text(
                        t(regionLabelKey(region)),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                    codes.forEach { code ->
                        DropdownMenuItem(text = { Text("${countryFlag(code)} ${displayName(code)}") }, onClick = { pick(code) })
                    }
                }
            } else {
                COUNTRIES.filter { displayName(it.code).contains(q, ignoreCase = true) }.forEach { c ->
                    DropdownMenuItem(text = { Text("${countryFlag(c.code)} ${displayName(c.code)}") }, onClick = { pick(c.code) })
                }
            }
        }
    }
}

/** A small icon per option makes it much faster to spot the right entry in a
 * 20-item dropdown than reading text alone — scoped to this one attribute
 * (not baked into the shared attrs.opts.* strings) since MACHINE_TYPE is the
 * only attribute that needs it. Mirrors web's machineTypeIcon(). */
private val MACHINE_TYPE_ICONS = mapOf(
    "FORKLIFT" to "📦", "EXCAVATOR" to "⛏️", "BULLDOZER" to "🚜", "CRANE" to "🏗️",
    "CONCRETE_MIXER" to "🧱", "CONCRETE_PUMP" to "🚰", "ROAD_ROLLER" to "🛣️",
    "PLATE_COMPACTOR" to "🚧", "AERIAL_PLATFORM" to "🪜", "SCISSOR_LIFT" to "⬆️",
    "SCAFFOLDING" to "🧗", "GENERATOR" to "🔌", "COMPRESSOR" to "💨",
    "WELDING_MACHINE" to "🔥", "WATER_PUMP" to "💧", "CHAINSAW" to "🪚",
    "LAWN_MOWER" to "🌱", "POWER_TOOLS" to "🛠️", "CLEANING_MACHINE" to "🧹", "OTHER" to "🔩"
)

private fun machineTypeIcon(code: String): String = MACHINE_TYPE_ICONS[code]?.let { "$it " } ?: ""

@Composable
private fun AttributeField(def: AttributeDefinitionDto, viewModel: DeposerAnnonceViewModel) {
    Column {
        Row {
            Text(tCatalog("attrs.${def.code}", def.code), fontSize = 13.sp, color = TextMuted)
            if (def.required) Text(" *", fontSize = 13.sp, color = Primary)
        }
        Spacer(Modifier.height(4.dp))
        when {
            def.code == "PROFESSION" -> {
                val industry = viewModel.attrTextValue("INDUSTRY")
                val options = JOB_PROFESSIONS_BY_SECTOR[industry] ?: JOB_PROFESSION_CODES
                TextAutocompleteField(
                    value = viewModel.attrTextValue(def.code),
                    onValueChange = { viewModel.setAttrText(def.code, it) },
                    options = options,
                    labelPrefix = "job_professions."
                )
            }
            def.type == "TEXT" -> OutlinedTextField(
                value = viewModel.attrTextValue(def.code),
                onValueChange = { viewModel.setAttrText(def.code, it) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, cursorColor = Primary)
            )
            def.type == "NUMBER" -> OutlinedTextField(
                value = viewModel.attrTextValue(def.code),
                onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) viewModel.setAttrText(def.code, it) },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, cursorColor = Primary)
            )
            def.type == "BOOLEAN" -> {
                val value = viewModel.attrBoolValue(def.code)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(true to t("common.yes"), false to t("common.no")).forEach { (v, label) ->
                        val selected = value == v
                        Box(
                            modifier = Modifier
                                .clickable { viewModel.setAttrBool(def.code, v) }
                                .background(if (selected) PrimaryLight else WhiteColor, RoundedCornerShape(999.dp))
                                .border(1.dp, if (selected) Primary else BorderColor, RoundedCornerShape(999.dp))
                                .padding(horizontal = 18.dp, vertical = 8.dp)
                        ) {
                            Text(label, color = if (selected) Primary else TextPrimary, fontSize = 13.sp)
                        }
                    }
                }
            }
            def.code == "MACHINE_TYPE" -> {
                var expanded by remember { mutableStateOf(false) }
                val value = viewModel.attrTextValue(def.code)
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = if (value.isEmpty()) "" else "${machineTypeIcon(value)}${tCatalog("attrs.opts.$value", value)}",
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Sélectionner…") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, cursorColor = Primary)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        def.options.forEach { opt ->
                            DropdownMenuItem(text = { Text("${machineTypeIcon(opt)}${tCatalog("attrs.opts.$opt", opt)}") }, onClick = { viewModel.setAttrText(def.code, opt); expanded = false })
                        }
                    }
                }
            }
            def.type == "SELECT" -> {
                var expanded by remember { mutableStateOf(false) }
                val value = viewModel.attrTextValue(def.code)
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = if (value.isEmpty()) "" else tCatalog("attrs.opts.$value", value),
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Sélectionner…") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, cursorColor = Primary)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        def.options.forEach { opt ->
                            DropdownMenuItem(text = { Text(tCatalog("attrs.opts.$opt", opt)) }, onClick = { viewModel.setAttrText(def.code, opt); expanded = false })
                        }
                    }
                }
            }
            def.type == "MULTI_SELECT" -> {
                val selected = viewModel.attrMultiValue(def.code)
                Column {
                    def.options.forEach { opt ->
                        val checked = opt in selected
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { viewModel.toggleAttrMulti(def.code, opt) }.padding(vertical = 4.dp)
                        ) {
                            Checkbox(checked = checked, onCheckedChange = { viewModel.toggleAttrMulti(def.code, opt) }, colors = CheckboxDefaults.colors(checkedColor = Primary))
                            Text(tCatalog("attrs.opts.$opt", opt), fontSize = 13.sp, color = TextPrimary)
                        }
                    }
                }
            }
            def.type == "DATE" -> {
                var showPicker by remember { mutableStateOf(false) }
                val value = viewModel.attrTextValue(def.code)
                Box {
                    OutlinedTextField(
                        value = value,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("jj/mm/aaaa") },
                        trailingIcon = { Icon(Icons.Filled.CalendarMonth, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, cursorColor = Primary)
                    )
                    // A .clickable{} straight on a readOnly OutlinedTextField fires
                    // unreliably — the field consumes the tap for its own focus
                    // first (see CLAUDE.md). A transparent overlay sibling works.
                    Box(modifier = Modifier.matchParentSize().clickable { showPicker = true })
                }
                if (showPicker) {
                    val initialMillis = value.toLocalDateOrNull()?.atStartOfDay(java.time.ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
                    val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
                    DatePickerDialog(
                        onDismissRequest = { showPicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                pickerState.selectedDateMillis?.let { millis ->
                                    val date = java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneOffset.UTC).toLocalDate()
                                    viewModel.setAttrText(def.code, date.toString())
                                }
                                showPicker = false
                            }) { Text(t("common.select")) }
                        },
                        dismissButton = { TextButton(onClick = { showPicker = false }) { Text(t("common.cancel")) } }
                    ) {
                        DatePicker(state = pickerState)
                    }
                }
            }
        }
    }
}

private fun String.toLocalDateOrNull(): java.time.LocalDate? =
    try { java.time.LocalDate.parse(this) } catch (e: java.time.format.DateTimeParseException) { null }

@Composable
private fun PhotosStep(viewModel: DeposerAnnonceViewModel, onPickPhotos: () -> Unit) {
    Text(t("deposer.photos_title"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
    Spacer(Modifier.height(4.dp))
    Text("${t("deposer.photos_sub_prefix")} ${viewModel.maxPhotos} ${t("deposer.photos_sub_suffix")}", color = TextMuted, fontSize = 13.sp)
    Spacer(Modifier.height(12.dp))

    Row(
        modifier = Modifier
            .clickable { viewModel.updatePremium(!viewModel.isPremium) }
            .background(if (viewModel.isPremium) GoldLight else WhiteColor, RoundedCornerShape(999.dp))
            .border(1.dp, if (viewModel.isPremium) Gold else BorderColor, RoundedCornerShape(999.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = viewModel.isPremium, onCheckedChange = { viewModel.updatePremium(it) }, colors = CheckboxDefaults.colors(checkedColor = Gold))
        Text(t("deposer.premium_toggle"), fontSize = 13.sp, color = TextPrimary)
    }
    Spacer(Modifier.height(16.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = viewModel.photos.size < viewModel.maxPhotos, onClick = onPickPhotos)
            .background(PrimaryLight, RoundedCornerShape(14.dp))
            .border(1.dp, Primary, RoundedCornerShape(14.dp))
            .padding(20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.AddAPhoto, contentDescription = null, tint = Primary)
        Spacer(Modifier.width(8.dp))
        Text(t("deposer.upload_hint"), color = Primary, fontWeight = FontWeight.SemiBold)
    }

    if (viewModel.photos.isNotEmpty()) {
        Spacer(Modifier.height(16.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.heightIn(max = 600.dp)
        ) {
            items(viewModel.photos.size) { index ->
                val photo = viewModel.photos[index]
                Box(modifier = Modifier.aspectRatio(1f)) {
                    AsyncImage(
                        model = photo.previewModel,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp))
                    )
                    if (index == 0) {
                        Box(
                            modifier = Modifier.align(Alignment.BottomStart).padding(4.dp)
                                .background(Primary, RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                        ) { Text(t("deposer.photo_main"), color = Color.White, fontSize = 9.sp) }
                    } else {
                        IconButton(
                            onClick = { viewModel.makePrimary(index) },
                            modifier = Modifier.align(Alignment.BottomStart).size(24.dp)
                                .background(WhiteColor.copy(alpha = 0.85f), CircleShape)
                        ) { Icon(Icons.Filled.StarBorder, contentDescription = "Définir comme principale", tint = Gold, modifier = Modifier.size(14.dp)) }
                    }
                    IconButton(
                        onClick = { viewModel.removePhoto(index) },
                        modifier = Modifier.align(Alignment.TopEnd).size(24.dp)
                            .background(WhiteColor.copy(alpha = 0.85f), CircleShape)
                    ) { Icon(Icons.Filled.Close, contentDescription = "Retirer", tint = TextPrimary, modifier = Modifier.size(14.dp)) }
                }
            }
        }
    }
    Spacer(Modifier.height(12.dp))
    Text(t("deposer.skip_note"), fontSize = 12.sp, color = TextMuted)
}

@Composable
private fun ContactStep(viewModel: DeposerAnnonceViewModel) {
    val form = viewModel.form
    Text(t("deposer.contact_title"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
    Spacer(Modifier.height(12.dp))
    Text(t("deposer.label_phone"), style = MaterialTheme.typography.labelLarge)
    Spacer(Modifier.height(4.dp))
    com.soukmar.app.ui.components.PhoneInputField(value = form.phone, onValueChange = { v -> viewModel.updateForm { f -> f.copy(phone = v) } })
    Spacer(Modifier.height(12.dp))
    Text(t("deposer.label_whatsapp"), style = MaterialTheme.typography.labelLarge)
    Spacer(Modifier.height(4.dp))
    com.soukmar.app.ui.components.PhoneInputField(value = form.whatsapp, onValueChange = { v -> viewModel.updateForm { f -> f.copy(whatsapp = v) } })
    Spacer(Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth().clickable { viewModel.updateForm { f -> f.copy(showPhone = !f.showPhone) } },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = form.showPhone, onCheckedChange = { viewModel.updateForm { f -> f.copy(showPhone = it) } }, colors = CheckboxDefaults.colors(checkedColor = Primary))
        Text(t("deposer.show_phone_toggle"), fontSize = 13.sp, color = TextPrimary)
    }
    Spacer(Modifier.height(4.dp))
    Text(t("deposer.show_phone_hint"), fontSize = 12.sp, color = TextMuted)

    Spacer(Modifier.height(20.dp))
    Column(
        modifier = Modifier.fillMaxWidth().background(WhiteColor, RoundedCornerShape(12.dp)).border(1.dp, BorderColor, RoundedCornerShape(12.dp)).padding(14.dp)
    ) {
        Text(t("deposer.summary_title"), fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        val cat = categoryConfig(form.category)
        val catLabel = cat?.let { tCatalog("cats.${it.value}", it.value) } ?: "—"
        SummaryRow(t("deposer.summary_cat"), "${cat?.emoji ?: ""} $catLabel")
        viewModel.subcategories.find { it.id == form.subcategoryId }?.let {
            SummaryRow(t("deposer.summary_subcat"), tCatalog("subcats.${it.code}", it.code))
        }
        if (form.condition.isNotEmpty()) SummaryRow(t("deposer.label_condition"), if (form.condition == "NEW") "Neuf" else "Occasion")
        SummaryRow(t("deposer.summary_listing_title"), form.title.ifEmpty { "—" })
        SummaryRow(t("deposer.summary_price"), if (form.price.isNotEmpty()) "${form.price} ${form.derivedCurrency}" else t("deposer.negotiate"))
        val uiLocale = localeForLang(LocalI18n.current.currentLang)
        SummaryRow(t("deposer.label_country"), "${countryFlag(form.country)} ${Locale("", form.country).getDisplayCountry(uiLocale)}")
        SummaryRow(t("deposer.summary_city"), form.city.ifEmpty { "—" })
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = TextMuted)
        Text(value, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}
