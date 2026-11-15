package com.soukmar.app.ui.screens.deposerannonce

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.soukmar.app.data.remote.dto.AiListingDraftDto
import com.soukmar.app.ui.i18n.t
import com.soukmar.app.ui.i18n.tCatalog
import com.soukmar.app.ui.theme.BorderColor
import com.soukmar.app.ui.theme.ErrorColor
import com.soukmar.app.ui.theme.Gold
import com.soukmar.app.ui.theme.GoldLight
import com.soukmar.app.ui.theme.Primary
import com.soukmar.app.ui.theme.PrimaryLight
import com.soukmar.app.ui.theme.SuccessColor
import com.soukmar.app.ui.theme.TextMuted
import com.soukmar.app.ui.theme.TextPrimary
import com.soukmar.app.ui.theme.WhiteColor
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull

/**
 * "Create the listing with AI" (mirrors the web's app-ai-listing-assistant). Sits above the wizard steps, so it
 * stays visible on every step; its state lives in [DeposerAnnonceViewModel] and survives back/next. The photos
 * are the form's own photo list. Nothing is written into the form before "apply suggestion".
 * Not rendered at all while the server has no AI machine configured.
 */
@Composable
fun AiAssistantCard(viewModel: DeposerAnnonceViewModel, onPickPhotos: () -> Unit) {
    if (!viewModel.aiConfigured) return
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
            .clip(shape)
            .border(1.5.dp, Gold, shape)
            .background(WhiteColor)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().background(GoldLight).clickable { viewModel.toggleAi() }.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(t("ai.cta"), fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                Text(
                    if (viewModel.aiDraft != null && !viewModel.aiOpen) t("ai.has_result") else t("ai.cta_sub"),
                    color = TextMuted, fontSize = 12.sp
                )
            }
            if (viewModel.aiLoading) {
                CircularProgressIndicator(color = Gold, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
            }
            Icon(if (viewModel.aiOpen) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = Gold)
        }

        viewModel.aiNotice?.let {
            Text(it, color = SuccessColor, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
        }

        if (viewModel.aiOpen) {
            HorizontalDivider(color = BorderColor)
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (viewModel.aiAvailable == false) {
                    Notice(t("ai.unavailable"), warn = true) {
                        TextButton(onClick = { viewModel.checkAiStatus() }) { Text(t("ai.retry_status"), color = Primary, fontSize = 12.sp) }
                    }
                }
                Text(t("ai.photos_hint"), color = TextMuted, fontSize = 13.sp)

                if (viewModel.photos.isNotEmpty()) {
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        viewModel.photos.forEachIndexed { index, photo ->
                            Box(modifier = Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)).border(1.dp, BorderColor, RoundedCornerShape(12.dp))) {
                                AsyncImage(model = photo.previewModel, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(72.dp))
                                Box(
                                    modifier = Modifier.align(Alignment.TopEnd).padding(3.dp).size(22.dp).clip(CircleShape)
                                        .background(TextPrimary.copy(alpha = 0.75f))
                                        .clickable(enabled = !viewModel.aiLoading) { viewModel.removePhoto(index) },
                                    contentAlignment = Alignment.Center
                                ) { Icon(Icons.Filled.Close, contentDescription = t("ai.photo_remove"), tint = WhiteColor, modifier = Modifier.size(13.dp)) }
                            }
                        }
                    }
                }

                if (viewModel.photos.size < viewModel.maxPhotos) {
                    OutlinedButton(onClick = onPickPhotos, enabled = !viewModel.aiLoading, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Filled.AddAPhoto, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (viewModel.photos.isEmpty()) t("ai.add_photos") else t("ai.add_more"))
                    }
                }
                if (viewModel.aiDraft == null) {
                    AiPrimaryButton(t("ai.analyse"), enabled = viewModel.aiUris.isNotEmpty() && !viewModel.aiLoading && viewModel.aiAvailable != false) { viewModel.aiAnalyse() }
                }
                if (viewModel.aiTooManyPhotos) {
                    Text(t("ai.max_note", "n" to viewModel.aiMaxImages.toString()), color = TextMuted, fontSize = 12.sp)
                }

                if (viewModel.aiLoading) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(com.soukmar.app.ui.theme.BgColor).padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(color = Gold, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                        Column {
                            Text("${t("ai.analysing")} (${viewModel.aiElapsed} s)", fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 13.sp)
                            Text(t("ai.analysing_hint"), color = TextMuted, fontSize = 12.sp)
                        }
                    }
                }

                viewModel.aiError?.let { Notice(it, warn = false) }

                viewModel.aiDraft?.let { draft -> AiResult(viewModel, draft) }

                TextButton(onClick = { viewModel.toggleAi() }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text(t("ai.close"), color = TextMuted, fontSize = 13.sp)
                }
            }
        }
    }

    // A new result never silently replaces texts edited in the preview; "apply" never silently replaces
    // a title or description the user wrote in the form.
    viewModel.aiConfirm?.let { kind ->
        AlertDialog(
            onDismissRequest = { viewModel.aiResolveConfirm(replace = false) },
            text = { Text(if (kind == AiConfirm.EDITED_PREVIEW) t("ai.confirm_overwrite_edit") else t("ai.confirm_overwrite_form")) },
            confirmButton = { TextButton(onClick = { viewModel.aiResolveConfirm(replace = true) }) { Text(t("ai.replace"), color = Primary) } },
            dismissButton = { TextButton(onClick = { viewModel.aiResolveConfirm(replace = false) }) { Text(t("ai.keep_mine")) } }
        )
    }
}

@Composable
private fun AiResult(viewModel: DeposerAnnonceViewModel, draft: AiListingDraftDto) {
    HorizontalDivider(color = BorderColor)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(t("ai.result_title"), fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
        val (label, color) = when {
            draft.confidence >= 0.75 -> t("ai.conf_high") to SuccessColor
            draft.confidence >= 0.5 -> t("ai.conf_medium") to Gold
            else -> t("ai.conf_low") to ErrorColor
        }
        Text("${t("ai.confidence")}: $label", color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
    Text(t("ai.result_note"), color = TextMuted, fontSize = 12.sp)

    if (draft.confidence < 0.5) Notice(t("ai.uncertain"), warn = true)
    if (viewModel.aiPhotosChanged) Notice(t("ai.photos_changed"), warn = true)
    if (draft.cached) Text(t("ai.cached"), color = TextMuted, fontSize = 12.sp)

    Fact(t("ai.product"), draft.productName)

    OutlinedTextField(
        value = viewModel.aiTitle,
        onValueChange = { if (it.length <= 100) viewModel.aiTitle = it },
        label = { Text(t("deposer.label_title")) },
        supportingText = { Text("${viewModel.aiTitle.length}/100 ${t("deposer.chars")}") },
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
    )
    OutlinedTextField(
        value = viewModel.aiDescription,
        onValueChange = { viewModel.aiDescription = it },
        label = { Text(t("deposer.label_desc")) },
        minLines = 5,
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
    )

    val choices = listOf(draft.suggestedCategory) + draft.alternativeCategories
    Label(if (choices.size > 1) t("ai.category_choose") else t("ai.category"))
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        choices.forEachIndexed { i, c ->
            val text = tCatalog("cats.${c.category}", c.category) + (c.subcategoryCode?.let { " › " + tCatalog("subcats.$it", it) } ?: "")
            FilterChip(
                selected = viewModel.aiSelectedCategory == i,
                onClick = { viewModel.aiSelectedCategory = i },
                label = { Text(text) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryLight, selectedLabelColor = Primary)
            )
        }
    }

    Label(t("ai.attributes"))
    val attrs = if (viewModel.aiSelectedCategory == 0) draft.attributes else emptyMap()
    if (attrs.isEmpty()) {
        Text(t("ai.no_attributes"), color = TextMuted, fontSize = 12.sp)
    } else {
        attrs.forEach { (code, value) -> Fact(tCatalog("attrs.$code", code), attrValueLabel(value)) }
    }

    Label(t("ai.missing"))
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Bullet(t("ai.missing_default"))
        if (draft.brand.isNullOrBlank()) Bullet(t("ai.missing_brand"))
        if (viewModel.aiSelectedCategory == 0) draft.missingAttributes.forEach { Bullet(tCatalog("attrs.$it", it)) }
        draft.missingInformation.forEach { Bullet(it) }
    }

    if (draft.keywords.isNotEmpty()) {
        Label(t("ai.keywords"))
        Text(draft.keywords.joinToString(" · "), color = TextMuted, fontSize = 13.sp)
    }

    AiPrimaryButton(t("ai.apply"), enabled = !viewModel.aiLoading && viewModel.aiTitle.isNotBlank() && viewModel.aiDescription.isNotBlank()) { viewModel.aiApply() }
    if (viewModel.aiPhotosChanged) {
        OutlinedButton(onClick = { viewModel.aiAnalyse() }, enabled = !viewModel.aiLoading && viewModel.aiUris.isNotEmpty(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text(t("ai.reanalyse")) }
    }
    OutlinedButton(onClick = { viewModel.aiAnalyse(force = true) }, enabled = !viewModel.aiLoading && viewModel.aiUris.isNotEmpty(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text(t("ai.regenerate")) }
}

/** Option codes (SELECT) have a translation, free text (brand, model) is shown as it is. */
@Composable
private fun attrValueLabel(value: JsonElement): String {
    val p = value as? JsonPrimitive ?: return value.toString()
    p.booleanOrNull?.let { if (!p.isString) return if (it) t("common.yes") else t("common.no") }
    val key = "attrs.opts.${p.content}"
    val label = t(key)
    return if (label == key) p.content else label
}

@Composable
private fun AiPrimaryButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Primary)
    ) { Text(text, fontWeight = FontWeight.SemiBold) }
}

@Composable
private fun Label(text: String) {
    Text(text, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 13.sp)
}

@Composable
private fun Fact(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(label, color = TextMuted, fontSize = 13.sp, modifier = Modifier.weight(0.4f))
        Text(value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.6f))
    }
}

@Composable
private fun Bullet(text: String) {
    Text("• $text", color = TextPrimary, fontSize = 13.sp)
}

@Composable
private fun Notice(text: String, warn: Boolean, action: (@Composable () -> Unit)? = null) {
    val bg: Color = if (warn) GoldLight else PrimaryLight
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(bg).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Filled.Warning, contentDescription = null, tint = if (warn) Gold else ErrorColor, modifier = Modifier.size(16.dp))
        Text(text, color = if (warn) TextPrimary else ErrorColor, fontSize = 13.sp, modifier = Modifier.weight(1f))
        action?.invoke()
    }
}
