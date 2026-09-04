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
import com.soukmar.app.ui.model.CATEGORIES
import com.soukmar.app.ui.model.MOROCCO_CITIES
import com.soukmar.app.ui.model.categoryConfig
import com.soukmar.app.ui.model.humanizeCode
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
                title = { Text(if (viewModel.isEdit) "Modifier l'annonce" else "Déposer une annonce") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Retour") } }
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
        Text("Connexion requise", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        Text("Connectez-vous pour déposer une annonce.", color = TextMuted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        PrimaryButton(text = "Se connecter", onClick = onRequireLogin, modifier = Modifier.fillMaxWidth(0.7f))
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
            if (isEdit) "Annonce mise à jour !" else "Annonce publiée !",
            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary
        )
        Spacer(Modifier.height(8.dp))
        Text("Elle est maintenant visible sur SoukMar.", color = TextMuted)
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
                OutlinedButton(onClick = { viewModel.goBack() }, enabled = !viewModel.loading) { Text("Retour") }
            } else {
                Spacer(Modifier.width(1.dp))
            }
            if (viewModel.step < DEPOSER_STEPS.size - 1) {
                Button(
                    onClick = { viewModel.goNext() },
                    enabled = viewModel.canNext,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.White)
                ) { Text("Suivant") }
            } else {
                Button(
                    onClick = { viewModel.publish(onPublished) },
                    enabled = viewModel.canNext && !viewModel.loading,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.White)
                ) {
                    if (viewModel.uploading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp)); Text("Envoi des photos…")
                    } else if (viewModel.loading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp)); Text("Publication…")
                    } else {
                        Text(if (viewModel.isEdit) "Mettre à jour" else "Publier l'annonce")
                    }
                }
            }
        }
    }
}

@Composable
private fun Stepper(step: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier.horizontalScroll(rememberScrollState()), verticalAlignment = Alignment.CenterVertically) {
        DEPOSER_STEPS.forEachIndexed { index, label ->
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
            Text(label, fontSize = 12.sp, color = if (active) TextPrimary else TextMuted, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal)
            if (index < DEPOSER_STEPS.size - 1) {
                Box(modifier = Modifier.width(16.dp).height(1.dp).background(if (done) Primary else BorderColor))
            }
        }
    }
}

@Composable
private fun CategoryStep(viewModel: DeposerAnnonceViewModel) {
    Text("Choisissez une catégorie", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
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
                Text(cat.emoji, fontSize = 26.sp)
                Spacer(Modifier.height(6.dp))
                Text(cat.label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (selected) Primary else cat.fg, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }
}

@Composable
private fun SubcategoryStep(viewModel: DeposerAnnonceViewModel) {
    Text("Choisissez une sous-catégorie", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
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
                Text(humanizeCode(sub.code), fontSize = 13.sp, color = if (selected) Primary else TextPrimary, fontWeight = FontWeight.Medium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }
}

@Composable
private fun DetailsStep(viewModel: DeposerAnnonceViewModel) {
    val form = viewModel.form
    Text("Détails de l'annonce", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
    Spacer(Modifier.height(12.dp))

    OutlinedTextField(
        value = form.title,
        onValueChange = { if (it.length <= 100) viewModel.updateForm { f -> f.copy(title = it) } },
        label = { Text("Titre de l'annonce") },
        supportingText = { Text("${form.title.length}/100 caractères") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, cursorColor = Primary)
    )
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(
        value = form.description,
        onValueChange = { viewModel.updateForm { f -> f.copy(description = it) } },
        label = { Text("Description") },
        minLines = 4,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, cursorColor = Primary)
    )
    Spacer(Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = form.price,
            onValueChange = { if (it.all { c -> c.isDigit() }) viewModel.updateForm { f -> f.copy(price = it) } },
            label = { Text("Prix") },
            placeholder = { Text("0") },
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, cursorColor = Primary)
        )
        var currencyExpanded by remember { mutableStateOf(false) }
        Box(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = form.currency,
                onValueChange = {},
                readOnly = true,
                label = { Text("Devise") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, cursorColor = Primary)
            )
            // A readOnly OutlinedTextField still consumes taps for its own
            // focus/cursor handling, so a clickable on the field itself never
            // fires — an invisible overlay on top is what actually gets the tap.
            Box(modifier = Modifier.matchParentSize().clickable { currencyExpanded = true })
            DropdownMenu(expanded = currencyExpanded, onDismissRequest = { currencyExpanded = false }) {
                listOf("MAD", "EUR", "USD").forEach { cur ->
                    DropdownMenuItem(text = { Text(cur) }, onClick = { viewModel.updateForm { f -> f.copy(currency = cur) }; currencyExpanded = false })
                }
            }
        }
    }
    Spacer(Modifier.height(12.dp))
    CityDropdown(selected = form.city, onSelect = { viewModel.updateForm { f -> f.copy(city = it) } })

    if (viewModel.showCondition) {
        Spacer(Modifier.height(12.dp))
        Text("État", fontSize = 13.sp, color = TextMuted)
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

@Composable
private fun CityDropdown(selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    Box {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Ville") },
            placeholder = { Text("Choisir une ville") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, cursorColor = Primary)
        )
        Box(modifier = Modifier.matchParentSize().clickable { expanded = true })
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.heightIn(max = 320.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Rechercher…") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, cursorColor = Primary)
            )
            MOROCCO_CITIES.filter { it.contains(query, ignoreCase = true) }.take(60).forEach { city ->
                DropdownMenuItem(text = { Text(city) }, onClick = { onSelect(city); expanded = false; query = "" })
            }
        }
    }
}

@Composable
private fun AttributeField(def: AttributeDefinitionDto, viewModel: DeposerAnnonceViewModel) {
    Column {
        Row {
            Text(humanizeCode(def.code), fontSize = 13.sp, color = TextMuted)
            if (def.required) Text(" *", fontSize = 13.sp, color = Primary)
        }
        Spacer(Modifier.height(4.dp))
        when (def.type) {
            "TEXT" -> OutlinedTextField(
                value = viewModel.attrTextValue(def.code),
                onValueChange = { viewModel.setAttrText(def.code, it) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, cursorColor = Primary)
            )
            "NUMBER" -> OutlinedTextField(
                value = viewModel.attrTextValue(def.code),
                onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) viewModel.setAttrText(def.code, it) },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, cursorColor = Primary)
            )
            "BOOLEAN" -> {
                val value = viewModel.attrBoolValue(def.code)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(true to "Oui", false to "Non").forEach { (v, label) ->
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
            "SELECT" -> {
                var expanded by remember { mutableStateOf(false) }
                val value = viewModel.attrTextValue(def.code)
                Box {
                    OutlinedTextField(
                        value = if (value.isEmpty()) "" else humanizeCode(value),
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Sélectionner…") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, cursorColor = Primary)
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { expanded = true })
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        def.options.forEach { opt ->
                            DropdownMenuItem(text = { Text(humanizeCode(opt)) }, onClick = { viewModel.setAttrText(def.code, opt); expanded = false })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotosStep(viewModel: DeposerAnnonceViewModel, onPickPhotos: () -> Unit) {
    Text("Photos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
    Spacer(Modifier.height(4.dp))
    Text("Ajoutez jusqu'à ${viewModel.maxPhotos} photos.", color = TextMuted, fontSize = 13.sp)
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
        Text("🌟 Annonce premium (jusqu'à 20 photos)", fontSize = 13.sp, color = TextPrimary)
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
        Text("Ajouter des photos", color = Primary, fontWeight = FontWeight.SemiBold)
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
                        ) { Text("Principale", color = Color.White, fontSize = 9.sp) }
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
    Text("Vous pouvez publier sans photo et en ajouter plus tard.", fontSize = 12.sp, color = TextMuted)
}

@Composable
private fun ContactStep(viewModel: DeposerAnnonceViewModel) {
    val form = viewModel.form
    Text("Contact", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(
        value = form.phone,
        onValueChange = { viewModel.updateForm { f -> f.copy(phone = it) } },
        label = { Text("Téléphone") },
        placeholder = { Text("+212 6 00 00 00 00") },
        singleLine = true,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, cursorColor = Primary)
    )
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(
        value = form.whatsapp,
        onValueChange = { viewModel.updateForm { f -> f.copy(whatsapp = it) } },
        label = { Text("WhatsApp") },
        placeholder = { Text("+212 6 00 00 00 00") },
        singleLine = true,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, cursorColor = Primary)
    )
    Spacer(Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth().clickable { viewModel.updateForm { f -> f.copy(showPhone = !f.showPhone) } },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = form.showPhone, onCheckedChange = { viewModel.updateForm { f -> f.copy(showPhone = it) } }, colors = CheckboxDefaults.colors(checkedColor = Primary))
        Text("📞 Afficher mon numéro publiquement", fontSize = 13.sp, color = TextPrimary)
    }
    Spacer(Modifier.height(4.dp))
    Text("Sinon, les acheteurs devront vous contacter par chat.", fontSize = 12.sp, color = TextMuted)

    Spacer(Modifier.height(20.dp))
    Column(
        modifier = Modifier.fillMaxWidth().background(WhiteColor, RoundedCornerShape(12.dp)).border(1.dp, BorderColor, RoundedCornerShape(12.dp)).padding(14.dp)
    ) {
        Text("Récapitulatif", fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        val cat = categoryConfig(form.category)
        SummaryRow("Catégorie", "${cat?.emoji ?: ""} ${cat?.label ?: "—"}")
        viewModel.subcategories.find { it.id == form.subcategoryId }?.let {
            SummaryRow("Sous-catégorie", humanizeCode(it.code))
        }
        if (form.condition.isNotEmpty()) SummaryRow("État", if (form.condition == "NEW") "Neuf" else "Occasion")
        SummaryRow("Titre", form.title.ifEmpty { "—" })
        SummaryRow("Prix", if (form.price.isNotEmpty()) "${form.price} ${form.currency}" else "À négocier")
        SummaryRow("Ville", form.city.ifEmpty { "—" })
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = TextMuted)
        Text(value, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}
