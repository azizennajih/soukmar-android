@file:OptIn(ExperimentalMaterial3Api::class)

package com.soukmar.app.ui.screens.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.soukmar.app.data.remote.dto.ModerationDetailDto
import com.soukmar.app.data.remote.dto.ModerationEventDto
import com.soukmar.app.data.remote.dto.ModerationQueueItemDto
import com.soukmar.app.data.remote.dto.ModerationSignalDto
import com.soukmar.app.ui.i18n.cityLabelT
import com.soukmar.app.ui.i18n.formatPricePartsT
import com.soukmar.app.ui.i18n.t
import com.soukmar.app.ui.i18n.tCatalog
import com.soukmar.app.ui.i18n.timeAgoT
import com.soukmar.app.ui.model.CATEGORIES
import com.soukmar.app.ui.model.COUNTRIES
import com.soukmar.app.ui.model.countryFlag
import com.soukmar.app.ui.theme.BorderColor
import com.soukmar.app.ui.theme.ErrorColor
import com.soukmar.app.ui.theme.Gold
import com.soukmar.app.ui.theme.GoldLight
import com.soukmar.app.ui.theme.Primary
import com.soukmar.app.ui.theme.PrimaryLight
import com.soukmar.app.ui.theme.TextMuted
import com.soukmar.app.ui.theme.TextPrimary
import com.soukmar.app.ui.theme.WhiteColor
import java.util.Locale

private val QUEUE_STATUSES = listOf("PENDING", "CHANGES_REQUESTED", "REJECTED", "SUSPENDED", "ACTIVE", "ALL")

@Composable
private fun statusLabel(status: String): String = when (status) {
    "ACTIVE" -> t("annonces.active")
    "RESERVED" -> t("annonces.reserved")
    "PENDING" -> t("annonces.pending")
    "SOLD" -> t("annonces.sold")
    "REJECTED" -> t("annonces.rejected")
    "EXPIRED" -> t("annonces.expired")
    "CHANGES_REQUESTED" -> t("annonces.changes_requested")
    "SUSPENDED" -> t("annonces.suspended")
    "ALL" -> t("moderation.f_all_status")
    else -> status
}

private fun statusColors(status: String): Pair<Color, Color> = when (status) {
    "ACTIVE" -> Color(0xFFDCFCE7) to Color(0xFF15803D)
    "SOLD" -> Color(0xFFDBEAFE) to Color(0xFF1D4ED8)
    "REJECTED", "SUSPENDED", "EXPIRED" -> Color(0xFFFEE2E2) to Color(0xFFB91C1C)
    else -> Color(0xFFFEF9C3) to Color(0xFFA16207)
}

@Composable
private fun StatusBadge(status: String) {
    val (bg, fg) = statusColors(status)
    Box(modifier = Modifier.background(bg, RoundedCornerShape(999.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) {
        Text(statusLabel(status), color = fg, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

private fun countryLabel(code: String): String = Locale("", code).displayCountry.ifBlank { code }

/** Highest severity among warnings: "high" > "medium" > "low", or null when there are none. */
private fun highestSeverity(signals: List<ModerationSignalDto>): String? = when {
    signals.any { it.severity == "high" } -> "high"
    signals.any { it.severity == "medium" } -> "medium"
    signals.isNotEmpty() -> "low"
    else -> null
}

@Composable
private fun signalText(code: String, fallback: String, params: Array<Pair<String, String>>): String {
    val key = "moderation.sig_$code"
    val text = t(key, *params)
    return if (text == key) fallback else text
}

/** Where the server text carries the actual finding (matched rule, AI categories) it is shown after the label. */
private fun signalDetail(s: ModerationSignalDto): String? = if (s.code == "KEYWORD" || s.code == "AI_FLAGGED") s.message.takeIf { it.isNotBlank() } else null

@Composable
fun ModerationPanel(
    onOpenListing: (String) -> Unit,
    viewModel: ModerationViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) { viewModel.start() }
    val snackbar = remember { SnackbarHostState() }
    val message = viewModel.messageKey?.let { t(it) }
    val extra = viewModel.extraMessageKey?.let { t(it) }
    LaunchedEffect(message) {
        if (message != null) {
            snackbar.showSnackbar(message)
            if (extra != null) snackbar.showSnackbar(extra)
            viewModel.clearMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (viewModel.detail != null) {
            BackHandler { viewModel.closeDetail() }
            ModerationDetail(viewModel, viewModel.detail!!, onOpenListing)
        } else if (viewModel.detailLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
        } else {
            ModerationQueue(viewModel)
        }
        SnackbarHost(snackbar, modifier = Modifier.align(Alignment.BottomCenter))
    }

    viewModel.confirmAction?.let { action ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelDecision() },
            text = {
                Text(
                    if (action == "approve") t("moderation.confirm_approve", "title" to (viewModel.detail?.listing?.title ?: ""))
                    else t("moderation.confirm_requeue")
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirm() }, enabled = !viewModel.submitting) {
                    Text(if (action == "approve") t("moderation.act_approve") else t("moderation.act_requeue"), color = Primary)
                }
            },
            dismissButton = { TextButton(onClick = { viewModel.cancelDecision() }) { Text(t("common.cancel")) } }
        )
    }

    viewModel.reasonAction?.let { action ->
        val title = when (action) {
            "reject" -> t("moderation.act_reject")
            "suspend" -> t("moderation.act_suspend")
            else -> t("moderation.act_request")
        }
        AlertDialog(
            onDismissRequest = { if (!viewModel.submitting) viewModel.cancelDecision() },
            title = { Text(title) },
            text = {
                Column {
                    Text(t("moderation.reason_label"), fontSize = 13.sp, color = TextMuted)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = viewModel.reason,
                        onValueChange = { if (it.length <= REASON_MAX) viewModel.reason = it },
                        placeholder = { Text(t("moderation.reason_ph")) },
                        minLines = 3,
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "${viewModel.reason.trim().length}/$REASON_MAX · ${t("moderation.reason_min", "n" to REASON_MIN.toString())}",
                        fontSize = 11.sp,
                        color = if (viewModel.reason.isNotEmpty() && !viewModel.reasonValid) ErrorColor else TextMuted
                    )
                    if (action == "reject") {
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = viewModel.allowResubmit, onCheckedChange = { viewModel.allowResubmit = it })
                            Text(t("moderation.allow_resubmit"), fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.submitReason() }, enabled = viewModel.reasonValid && !viewModel.submitting) {
                    Text(t("moderation.send_decision"), color = Primary)
                }
            },
            dismissButton = { TextButton(onClick = { viewModel.cancelDecision() }, enabled = !viewModel.submitting) { Text(t("common.cancel")) } }
        )
    }
}

// ── queue ────────────────────────────────────────────────────────────────────

@Composable
private fun ModerationQueue(viewModel: ModerationViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(t("moderation.subtitle"), color = TextMuted, fontSize = 12.sp)
        }
        viewModel.summary?.let { s ->
            item {
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryCard(s.pending.toString(), t("moderation.sum_pending"), highlight = s.pending > 0) { viewModel.selectStatus("PENDING") }
                    SummaryCard(s.highRiskPending.toString(), t("moderation.sum_high"), danger = s.highRiskPending > 0)
                    SummaryCard(s.changesRequested.toString(), t("moderation.sum_changes")) { viewModel.selectStatus("CHANGES_REQUESTED") }
                    SummaryCard(s.suspended.toString(), t("moderation.sum_suspended")) { viewModel.selectStatus("SUSPENDED") }
                    SummaryCard(s.decidedToday.toString(), t("moderation.sum_today"))
                }
            }
        }
        item {
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QUEUE_STATUSES.forEach { st ->
                    FilterChip(
                        selected = viewModel.status == st,
                        onClick = { viewModel.selectStatus(st) },
                        label = { Text(statusLabel(st)) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryLight, selectedLabelColor = Primary)
                    )
                }
            }
        }
        item {
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterDropdown(
                    current = if (viewModel.country.isBlank()) t("moderation.f_all_countries") else "${countryFlag(viewModel.country)} ${countryLabel(viewModel.country)}",
                    options = listOf("" to t("moderation.f_all_countries")) + COUNTRIES.map { it.code to "${countryFlag(it.code)} ${countryLabel(it.code)}" },
                    onSelect = { viewModel.selectCountry(it) }
                )
                FilterDropdown(
                    current = if (viewModel.category.isBlank()) t("moderation.f_all_categories") else tCatalog("cats.${viewModel.category}", viewModel.category),
                    options = listOf("" to t("moderation.f_all_categories")) + CATEGORIES.map { it.value to tCatalog("cats.${it.value}", it.value) },
                    onSelect = { viewModel.selectCategory(it) }
                )
                FilterDropdown(
                    current = when (viewModel.sort) {
                        "risk" -> t("moderation.sort_risk")
                        "newest" -> t("moderation.sort_newest")
                        else -> t("moderation.sort_oldest")
                    },
                    options = listOf(
                        "oldest" to t("moderation.sort_oldest"),
                        "risk" to t("moderation.sort_risk"),
                        "newest" to t("moderation.sort_newest")
                    ),
                    onSelect = { viewModel.selectSort(it) }
                )
            }
        }
        item {
            OutlinedTextField(
                value = viewModel.query,
                onValueChange = { viewModel.query = it },
                placeholder = { Text(t("moderation.f_search_ph"), fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { viewModel.search() }),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(t("moderation.count", "n" to viewModel.total.toString()), color = TextMuted, fontSize = 12.sp, modifier = Modifier.weight(1f))
                TextButton(onClick = { viewModel.resetFilters() }) { Text(t("moderation.f_reset"), fontSize = 12.sp) }
            }
        }

        when {
            viewModel.loading -> item { Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) } }
            viewModel.loadError -> item { Text(t("auth.generic_error"), color = ErrorColor) }
            viewModel.items.isEmpty() -> item { Text(t("moderation.empty"), color = TextMuted, modifier = Modifier.padding(24.dp)) }
            else -> {
                items(viewModel.items, key = { it.id }) { item -> QueueRow(item) { viewModel.openDetail(item.id) } }
                if (viewModel.page < viewModel.pages) {
                    item {
                        OutlinedButton(onClick = { viewModel.loadMore() }, enabled = !viewModel.loadingMore, modifier = Modifier.fillMaxWidth()) {
                            Text(if (viewModel.loadingMore) t("admin.loading") else t("moderation.next"))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(value: String, label: String, highlight: Boolean = false, danger: Boolean = false, onClick: (() -> Unit)? = null) {
    val bg = when { danger -> PrimaryLight; highlight -> GoldLight; else -> WhiteColor }
    val border = when { danger -> Primary; highlight -> Gold; else -> BorderColor }
    Column(
        modifier = Modifier
            .width(120.dp)
            .background(bg, RoundedCornerShape(12.dp))
            .border(1.dp, border, RoundedCornerShape(12.dp))
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(12.dp)
    ) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text(label, fontSize = 11.sp, color = TextMuted)
    }
}

@Composable
private fun FilterDropdown(current: String, options: List<Pair<String, String>>, onSelect: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { open = true }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
            Text(current, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = TextPrimary)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }, modifier = Modifier.heightIn(max = 360.dp)) {
            options.forEach { (value, label) ->
                DropdownMenuItem(text = { Text(label) }, onClick = { onSelect(value); open = false })
            }
        }
    }
}

@Composable
private fun QueueRow(item: ModerationQueueItemDto, onOpen: () -> Unit) {
    val price = if (item.priceType == "FREE") t("listing.free_badge") else item.price?.let { formatPricePartsT(it, item.currency).let { p -> "${p.first} ${p.second}" } } ?: t("listing.negotiate")
    val severity = highestSeverity(item.moderationSignals)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(WhiteColor, RoundedCornerShape(14.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onOpen)
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(modifier = Modifier.size(64.dp).clip(RoundedCornerShape(10.dp)).background(BorderColor)) {
            item.images.firstOrNull()?.let {
                AsyncImage(model = it, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(6.dp))
                StatusBadge(item.status)
            }
            Text(
                "${countryFlag(item.country)} ${cityLabelT(item.city)} · ${tCatalog("cats.${item.category}", item.category)} · $price",
                fontSize = 11.sp, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Text(
                "${item.user?.name ?: ""} · ${t("moderation.submitted", "time" to timeAgoT(item.submittedAt ?: item.createdAt))}",
                fontSize = 11.sp, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            if (!item.moderationReason.isNullOrBlank() && item.status != "PENDING") {
                Text(item.moderationReason, fontSize = 11.sp, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(2.dp))
            when (severity) {
                null -> Text(t("moderation.no_warnings"), fontSize = 11.sp, color = TextMuted)
                "high" -> Text("⚠ " + t("moderation.warnings", "n" to item.moderationSignals.size.toString()), fontSize = 11.sp, color = Primary, fontWeight = FontWeight.SemiBold)
                else -> Text("⚠ " + t("moderation.warnings", "n" to item.moderationSignals.size.toString()), fontSize = 11.sp, color = Gold, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ── detail ───────────────────────────────────────────────────────────────────

@Composable
private fun ModerationDetail(viewModel: ModerationViewModel, d: ModerationDetailDto, onOpenListing: (String) -> Unit) {
    val l = d.listing
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.closeDetail() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = t("moderation.back")) }
            StatusBadge(l.status)
            Spacer(Modifier.width(8.dp))
            Text("ID ${l.id}", fontSize = 10.sp, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }

        // Decision first: the reviewer should not have to scroll for the buttons.
        PanelCard { DecisionBlock(viewModel, d) }

        PanelCard {
            if (l.images.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(l.images) { url ->
                        AsyncImage(model = url, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(width = 220.dp, height = 165.dp).clip(RoundedCornerShape(10.dp)))
                    }
                }
            } else {
                Text(t("moderation.d_no_images"), color = TextMuted, fontSize = 13.sp)
            }
            Spacer(Modifier.height(10.dp))
            Text(l.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            val price = if (l.priceType == "FREE") t("listing.free_badge") else l.price?.let { formatPricePartsT(it, l.currency).let { p -> "${p.first} ${p.second}" } } ?: t("listing.negotiate")
            Text(price, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(
                "${tCatalog("cats.${l.category}", l.category)} · ${countryFlag(l.country)} ${cityLabelT(l.city)}, ${countryLabel(l.country)}",
                fontSize = 12.sp, color = TextMuted
            )
            Spacer(Modifier.height(8.dp))
            Text(l.description, fontSize = 14.sp, color = TextPrimary)
            if (!l.phone.isNullOrBlank() || !l.whatsapp.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(t("moderation.d_contact"), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextMuted)
                l.phone?.takeIf { it.isNotBlank() }?.let { Text("${t("moderation.s_phone")}: $it", fontSize = 13.sp) }
                l.whatsapp?.takeIf { it.isNotBlank() }?.let { Text("WhatsApp: $it", fontSize = 13.sp) }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { onOpenListing(l.id) }) { Text(t("moderation.d_preview")) }
        }

        PanelCard {
            SectionTitle(t("moderation.sig_title"))
            if (l.moderationSignals.isEmpty()) {
                Text(t("moderation.sig_none"), fontSize = 13.sp, color = TextMuted)
            } else {
                l.moderationSignals.forEach { SignalRow(it.severity, signalText(it.code, it.message, it.paramStrings()), signalDetail(it)) }
            }
            Text(t("moderation.sig_disclaimer"), fontSize = 11.sp, color = TextMuted, modifier = Modifier.padding(top = 6.dp))
            if (d.duplicates.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                SectionTitle(t("moderation.dup_title"))
                d.duplicates.forEach { dup ->
                    SignalRow(
                        dup.severity,
                        signalText(dup.code, dup.message, dup.paramStrings()),
                        dup.listing?.let { "${it.title} (${statusLabel(it.status)})" },
                        onClick = dup.listing?.let { other -> { viewModel.openDetail(other.id) } }
                    )
                }
            }
        }

        PanelCard {
            SectionTitle(t("moderation.s_title"))
            val s = d.seller
            InfoRow(t("moderation.s_name"), s.name)
            InfoRow(t("moderation.s_email"), s.email)
            s.phone?.takeIf { it.isNotBlank() }?.let { InfoRow(t("moderation.s_phone"), it) }
            s.createdAt?.let { InfoRow(t("moderation.s_since"), timeAgoT(it)) }
            InfoRow(t("moderation.s_email_ok"), t(if (s.emailVerified) "common.yes" else "common.no"))
            InfoRow(t("moderation.s_phone_ok"), t(if (s.phoneVerified) "common.yes" else "common.no"))
            InfoRow(t("moderation.s_id_ok"), t(if (s.idVerified) "common.yes" else "common.no"))
            InfoRow(t("moderation.s_total"), s.listingsTotal.toString())
            InfoRow(t("moderation.s_public"), s.listingsPublic.toString())
            InfoRow(t("moderation.s_rejected"), s.listingsRejected.toString(), warn = s.listingsRejected > 0)
            InfoRow(t("moderation.s_reports"), s.reportsAgainst.toString(), warn = s.reportsAgainst > 0)
        }

        PanelCard {
            SectionTitle(t("moderation.hist_title"))
            if (d.events.isEmpty()) Text(t("moderation.hist_empty"), fontSize = 13.sp, color = TextMuted)
            d.events.forEach { ev -> EventRow(ev, resending = viewModel.resendingEventId == ev.id, onResend = { viewModel.resendMail(ev.id) }) }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun DecisionBlock(viewModel: ModerationViewModel, d: ModerationDetailDto) {
    SectionTitle(t("moderation.act_title"))
    if (d.allowedActions.isEmpty()) Text(t("moderation.act_none"), fontSize = 13.sp, color = TextMuted)
    if (!d.listing.moderationReason.isNullOrBlank() && d.listing.status != "ACTIVE") {
        Text("${t("moderation.last_reason")} ${d.listing.moderationReason}", fontSize = 12.sp, color = TextMuted, modifier = Modifier.padding(bottom = 8.dp))
    }
    val busy = viewModel.submitting
    if ("approve" in d.allowedActions) {
        Button(onClick = { viewModel.askConfirm("approve") }, enabled = !busy, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text(t("moderation.act_approve"))
        }
    }
    if ("request_changes" in d.allowedActions) {
        OutlinedButton(onClick = { viewModel.startReason("request-changes") }, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text(t("moderation.act_request"))
        }
    }
    if ("reject" in d.allowedActions) {
        OutlinedButton(onClick = { viewModel.startReason("reject") }, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Close, contentDescription = null, tint = ErrorColor, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text(t("moderation.act_reject"), color = ErrorColor)
        }
    }
    if ("suspend" in d.allowedActions) {
        OutlinedButton(onClick = { viewModel.startReason("suspend") }, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Pause, contentDescription = null, tint = ErrorColor, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text(t("moderation.act_suspend"), color = ErrorColor)
        }
    }
    if ("requeue" in d.allowedActions) {
        TextButton(onClick = { viewModel.askConfirm("requeue") }, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Schedule, contentDescription = null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text(t("moderation.act_requeue"))
        }
    }
}

@Composable
private fun PanelCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(WhiteColor, RoundedCornerShape(14.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        content = content
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary, modifier = Modifier.padding(bottom = 4.dp))
}

@Composable
private fun InfoRow(label: String, value: String, warn: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 12.sp, color = TextMuted, modifier = Modifier.weight(1f))
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (warn) ErrorColor else TextPrimary, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
    }
}

@Composable
private fun SignalRow(severity: String, text: String, detail: String?, onClick: (() -> Unit)? = null) {
    val (bg, fg) = when (severity) {
        "high" -> PrimaryLight to Primary
        "medium" -> GoldLight to Gold
        else -> WhiteColor to TextMuted
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(8.dp))
            .border(1.dp, if (severity == "low") BorderColor else fg, RoundedCornerShape(8.dp))
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(t("moderation.sev_$severity").uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = fg)
        Column(modifier = Modifier.weight(1f)) {
            Text(text, fontSize = 12.sp, color = TextPrimary)
            if (!detail.isNullOrBlank()) Text(detail, fontSize = 11.sp, color = if (onClick != null) Primary else TextMuted)
        }
    }
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun EventRow(ev: ModerationEventDto, resending: Boolean, onResend: () -> Unit) {
    val role = t("moderation.actor_${ev.actorRole}")
    val who = if (!ev.actorName.isNullOrBlank() && (ev.actorRole == "ADMIN" || ev.actorRole == "MODERATOR")) "${ev.actorName} ($role)" else role
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(
            if (ev.fromStatus == null) t("moderation.ev_submitted") else t("moderation.ev_changed", "from" to statusLabel(ev.fromStatus), "to" to statusLabel(ev.toStatus)),
            fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary
        )
        Text("$who · ${timeAgoT(ev.createdAt)}", fontSize = 11.sp, color = TextMuted)
        if (!ev.reason.isNullOrBlank()) Text(ev.reason, fontSize = 12.sp, color = TextPrimary)
        if (!ev.emailKind.isNullOrBlank()) {
            val st = ev.emailStatus ?: "PENDING"
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("✉ " + t("moderation.mail_$st"), fontSize = 11.sp, color = if (st == "FAILED") ErrorColor else TextMuted, fontWeight = if (st == "FAILED") FontWeight.SemiBold else FontWeight.Normal)
                if (st == "FAILED" || st == "PENDING") {
                    TextButton(onClick = onResend, enabled = !resending, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)) {
                        Text(t("moderation.mail_resend"), fontSize = 11.sp)
                    }
                }
            }
            if (!ev.emailError.isNullOrBlank()) Text(ev.emailError, fontSize = 10.sp, color = ErrorColor)
        }
    }
}
