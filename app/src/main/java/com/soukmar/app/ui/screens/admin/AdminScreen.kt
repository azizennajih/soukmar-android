@file:OptIn(ExperimentalMaterial3Api::class)

package com.soukmar.app.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.soukmar.app.data.i18n.I18nRepository
import com.soukmar.app.data.remote.dto.AdminIdVerificationDto
import com.soukmar.app.data.remote.dto.AdminReportDto
import com.soukmar.app.data.remote.dto.BoostRequestDto
import com.soukmar.app.ui.i18n.LocalI18n
import com.soukmar.app.ui.i18n.formatPricePartsT
import com.soukmar.app.ui.i18n.t
import com.soukmar.app.ui.i18n.timeAgoT
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

private val FILTERS = listOf("PENDING", "RESOLVED", "DISMISSED", "ALL")
private val ID_VERIFICATION_FILTERS = listOf("PENDING", "APPROVED", "REJECTED", "ALL")
private val BOOST_REQUEST_FILTERS = listOf("PENDING", "APPROVED", "REJECTED", "ALL")

private fun statusLabel(status: String, i18n: I18nRepository): String = when (status) {
    "PENDING" -> i18n.t("admin.reports_status_pending")
    "RESOLVED" -> i18n.t("admin.reports_status_resolved")
    "DISMISSED" -> i18n.t("admin.reports_status_dismissed")
    else -> i18n.t("admin.filter_all")
}

/** Reuses the `admin.reports_status_*` keys for APPROVED/REJECTED too —
 * mirrors the web's `admin.component.html`, which literally builds the key
 * as `'admin.reports_status_' + f.toLowerCase()` for the ID-verification
 * filter pills instead of having a separate set of translations. */
private fun idVerificationStatusLabel(status: String, i18n: I18nRepository): String = when (status) {
    "PENDING" -> i18n.t("admin.reports_status_pending")
    "APPROVED" -> i18n.t("admin.reports_status_approved")
    "REJECTED" -> i18n.t("admin.reports_status_rejected")
    else -> i18n.t("admin.filter_all")
}

@Composable
fun AdminScreen(
    onBack: () -> Unit,
    onOpenListing: (String) -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) { viewModel.load() }
    val i18n = LocalI18n.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(t("admin.reports_title")) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour") } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            val tabIndex = when (viewModel.currentTab) {
                AdminTab.REPORTS -> 0
                AdminTab.ID_VERIFICATIONS -> 1
                AdminTab.BOOST_REQUESTS -> 2
            }
            TabRow(selectedTabIndex = tabIndex, containerColor = WhiteColor, contentColor = Primary) {
                Tab(
                    selected = viewModel.currentTab == AdminTab.REPORTS,
                    onClick = { viewModel.currentTab = AdminTab.REPORTS },
                    text = { Text(t("admin.reports_title")) },
                    icon = { Icon(Icons.Filled.Flag, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = viewModel.currentTab == AdminTab.ID_VERIFICATIONS,
                    onClick = { viewModel.currentTab = AdminTab.ID_VERIFICATIONS },
                    text = { Text(t("admin.tab_id_verifications")) },
                    icon = { Icon(Icons.Filled.Badge, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = viewModel.currentTab == AdminTab.BOOST_REQUESTS,
                    onClick = { viewModel.currentTab = AdminTab.BOOST_REQUESTS },
                    text = { Text(t("admin.tab_boost_requests")) },
                    icon = { Icon(Icons.Filled.RocketLaunch, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            when (viewModel.currentTab) {
              AdminTab.REPORTS -> {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FILTERS.forEach { f ->
                        FilterChip(
                            selected = viewModel.filter == f,
                            onClick = { viewModel.filter = f },
                            label = { Text("${statusLabel(f, i18n)} (${viewModel.countFor(f)})") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryLight, selectedLabelColor = Primary)
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    when {
                        viewModel.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
                        viewModel.loadError -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Impossible de charger les signalements.", color = TextMuted)
                        }
                        viewModel.filteredReports.isEmpty() -> EmptyState()
                        else -> LazyColumn(
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(viewModel.filteredReports, key = { it.id }) { report ->
                                ReportCard(
                                    report = report,
                                    onOpenListing = onOpenListing,
                                    onResolve = { viewModel.openAction(report, "RESOLVED") },
                                    onDismiss = { viewModel.openAction(report, "DISMISSED") }
                                )
                            }
                        }
                    }
                }
              }
              AdminTab.ID_VERIFICATIONS -> {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ID_VERIFICATION_FILTERS.forEach { f ->
                        FilterChip(
                            selected = viewModel.idVerificationFilter == f,
                            onClick = { viewModel.idVerificationFilter = f },
                            label = { Text("${idVerificationStatusLabel(f, i18n)} (${viewModel.idCountFor(f)})") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryLight, selectedLabelColor = Primary)
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    when {
                        viewModel.idVerificationsLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
                        viewModel.idVerificationsLoadError -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Impossible de charger les vérifications d'identité.", color = TextMuted)
                        }
                        viewModel.filteredIdVerifications.isEmpty() -> IdVerificationEmptyState()
                        else -> LazyColumn(
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(viewModel.filteredIdVerifications, key = { it.id }) { v ->
                                IdVerificationCard(
                                    v = v,
                                    onApprove = { viewModel.openIdAction(v, "APPROVED") },
                                    onReject = { viewModel.openIdAction(v, "REJECTED") }
                                )
                            }
                        }
                    }
                }
              }
              AdminTab.BOOST_REQUESTS -> {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BOOST_REQUEST_FILTERS.forEach { f ->
                        FilterChip(
                            selected = viewModel.boostRequestFilter == f,
                            onClick = { viewModel.boostRequestFilter = f },
                            label = { Text("${idVerificationStatusLabel(f, i18n)} (${viewModel.boostCountFor(f)})") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryLight, selectedLabelColor = Primary)
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    when {
                        viewModel.boostRequestsLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
                        viewModel.boostRequestsLoadError -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Impossible de charger les demandes de visibilité.", color = TextMuted)
                        }
                        viewModel.filteredBoostRequests.isEmpty() -> BoostRequestEmptyState()
                        else -> LazyColumn(
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(viewModel.filteredBoostRequests, key = { it.id }) { r ->
                                BoostRequestCard(
                                    r = r,
                                    onOpenListing = onOpenListing,
                                    onApprove = { viewModel.openBoostAction(r, "APPROVED") },
                                    onReject = { viewModel.openBoostAction(r, "REJECTED") }
                                )
                            }
                        }
                    }
                }
              }
            }
        }
    }

    viewModel.actionTarget?.let {
        val status = viewModel.actionStatus ?: return@let
        AlertDialog(
            onDismissRequest = { viewModel.cancelAction() },
            title = { Text(if (status == "RESOLVED") "Résoudre le signalement" else "Rejeter le signalement") },
            text = {
                Column {
                    Text(t("admin.reports_note_prompt"), color = TextMuted, fontSize = 12.sp)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = viewModel.actionNote,
                        onValueChange = { viewModel.actionNote = it },
                        placeholder = { Text(t("admin.reports_note_prompt")) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmAction() },
                    enabled = !viewModel.actionSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text(if (viewModel.actionSubmitting) "Envoi…" else "Confirmer")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelAction() }) { Text(t("common.cancel")) }
            }
        )
    }

    viewModel.idActionTarget?.let {
        val status = viewModel.idActionStatus ?: return@let
        AlertDialog(
            onDismissRequest = { viewModel.cancelIdAction() },
            title = { Text(if (status == "APPROVED") t("admin.id_verifications_approve") else t("admin.id_verifications_reject")) },
            text = {
                Column {
                    Text(t("admin.id_verification_note_prompt"), color = TextMuted, fontSize = 12.sp)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = viewModel.idActionNote,
                        onValueChange = { viewModel.idActionNote = it },
                        placeholder = { Text(t("admin.id_verification_note_prompt")) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmIdAction() },
                    enabled = !viewModel.idActionSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text(if (viewModel.idActionSubmitting) "Envoi…" else "Confirmer")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelIdAction() }) { Text(t("common.cancel")) }
            }
        )
    }

    viewModel.boostActionTarget?.let {
        val status = viewModel.boostActionStatus ?: return@let
        AlertDialog(
            onDismissRequest = { viewModel.cancelBoostAction() },
            title = { Text(if (status == "APPROVED") t("admin.boost_requests_approve") else t("admin.boost_requests_reject")) },
            text = {
                Column {
                    Text(t("admin.boost_request_note_prompt"), color = TextMuted, fontSize = 12.sp)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = viewModel.boostActionNote,
                        onValueChange = { viewModel.boostActionNote = it },
                        placeholder = { Text(t("admin.boost_request_note_prompt")) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmBoostAction() },
                    enabled = !viewModel.boostActionSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text(if (viewModel.boostActionSubmitting) "Envoi…" else "Confirmer")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelBoostAction() }) { Text(t("common.cancel")) }
            }
        )
    }
}

@Composable
private fun ReportCard(
    report: AdminReportDto,
    onOpenListing: (String) -> Unit,
    onResolve: () -> Unit,
    onDismiss: () -> Unit
) {
    val i18n = LocalI18n.current
    Column(
        modifier = Modifier.fillMaxWidth().background(WhiteColor, RoundedCornerShape(14.dp)).border(1.dp, BorderColor, RoundedCornerShape(14.dp)).padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val (bg, fg) = when (report.status) {
                "PENDING" -> GoldLight to Gold
                "RESOLVED" -> SuccessColor.copy(alpha = 0.12f) to SuccessColor
                else -> ErrorColor.copy(alpha = 0.1f) to ErrorColor
            }
            Box(modifier = Modifier.background(bg, RoundedCornerShape(999.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                Text(statusLabel(report.status, i18n), color = fg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(8.dp))
            Text(timeAgoT(report.createdAt), color = TextMuted, fontSize = 11.sp)
        }

        Spacer(Modifier.height(10.dp))
        Text(t("admin.reports_reporter"), color = TextMuted, fontSize = 11.sp)
        Text("${report.reporter?.name ?: "?"} · ${report.reporter?.email ?: ""}", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)

        Spacer(Modifier.height(8.dp))
        Text(t("admin.reports_reported"), color = TextMuted, fontSize = 11.sp)
        Text("${report.reported?.name ?: "?"} · ${report.reported?.email ?: ""}", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)

        report.listing?.let { listing ->
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onOpenListing(listing.id) }) {
                Icon(Icons.Filled.Link, contentDescription = null, tint = Primary, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(listing.title, color = Primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(10.dp))
        Text(report.reason, color = TextPrimary, fontSize = 14.sp, lineHeight = 20.sp)

        report.adminNote?.takeIf { it.isNotBlank() }?.let {
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Filled.Edit, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                Text(it, color = TextMuted, fontSize = 12.sp, lineHeight = 17.sp)
            }
        }

        if (report.status == "PENDING") {
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onResolve, colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                    Text(t("admin.reports_resolve"))
                }
                OutlinedButton(onClick = onDismiss) {
                    Text(t("admin.reports_dismiss"))
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.Flag, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
        Spacer(Modifier.height(12.dp))
        Text(t("admin.reports_empty"), color = TextMuted, textAlign = TextAlign.Center)
    }
}

/** Mirrors the web's ID-verification queue row: user ref, two Cloudinary
 * photo thumbnails (ID + selfie, loaded directly with Coil — no local
 * storage, same URLs the backend already returns), status badge, and
 * Approve/Reject buttons only while PENDING. */
@Composable
private fun IdVerificationCard(
    v: AdminIdVerificationDto,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val i18n = LocalI18n.current
    Column(
        modifier = Modifier.fillMaxWidth().background(WhiteColor, RoundedCornerShape(14.dp)).border(1.dp, BorderColor, RoundedCornerShape(14.dp)).padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val (bg, fg) = when (v.status) {
                "PENDING" -> GoldLight to Gold
                "APPROVED" -> SuccessColor.copy(alpha = 0.12f) to SuccessColor
                else -> ErrorColor.copy(alpha = 0.1f) to ErrorColor
            }
            Box(modifier = Modifier.background(bg, RoundedCornerShape(999.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                Text(idVerificationStatusLabel(v.status, i18n), color = fg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(8.dp))
            Text(timeAgoT(v.createdAt), color = TextMuted, fontSize = 11.sp)
        }

        Spacer(Modifier.height(10.dp))
        Text(t("admin.id_verifications_user"), color = TextMuted, fontSize = 11.sp)
        Text("${v.user?.name ?: "?"} · ${v.user?.email ?: ""}", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)

        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                AsyncImage(
                    model = v.idImageUrl,
                    contentDescription = t("admin.id_verifications_id_photo"),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(8.dp)).background(BorderColor)
                )
                Spacer(Modifier.height(4.dp))
                Text(t("admin.id_verifications_id_photo"), color = Primary, fontSize = 11.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                AsyncImage(
                    model = v.selfieImageUrl,
                    contentDescription = t("admin.id_verifications_selfie_photo"),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(8.dp)).background(BorderColor)
                )
                Spacer(Modifier.height(4.dp))
                Text(t("admin.id_verifications_selfie_photo"), color = Primary, fontSize = 11.sp)
            }
        }

        v.adminNote?.takeIf { it.isNotBlank() }?.let {
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Filled.Edit, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                Text(it, color = TextMuted, fontSize = 12.sp, lineHeight = 17.sp)
            }
        }

        if (v.status == "PENDING") {
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onApprove, colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                    Text(t("admin.id_verifications_approve"))
                }
                OutlinedButton(onClick = onReject) {
                    Text(t("admin.id_verifications_reject"))
                }
            }
        }
    }
}

@Composable
private fun IdVerificationEmptyState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.Badge, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
        Spacer(Modifier.height(12.dp))
        Text(t("admin.id_verifications_empty"), color = TextMuted, textAlign = TextAlign.Center)
    }
}

/** Mirrors the web admin's Boosts tab: listing/seller refs, the requested
 * tier ids (translated via boost.tier_<id>_name) + quoted price, status
 * badge, Approve/Reject only while PENDING. Approving is the only place
 * that actually activates the effects (see backend's applyBoostTiers) — the
 * request itself never touches money, since no payment processor exists yet. */
@Composable
private fun BoostRequestCard(
    r: BoostRequestDto,
    onOpenListing: (String) -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val i18n = LocalI18n.current
    val priceParts = formatPricePartsT(r.totalPrice, r.currency)
    Column(
        modifier = Modifier.fillMaxWidth().background(WhiteColor, RoundedCornerShape(14.dp)).border(1.dp, BorderColor, RoundedCornerShape(14.dp)).padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val (bg, fg) = when (r.status) {
                "PENDING" -> GoldLight to Gold
                "APPROVED" -> SuccessColor.copy(alpha = 0.12f) to SuccessColor
                else -> ErrorColor.copy(alpha = 0.1f) to ErrorColor
            }
            Box(modifier = Modifier.background(bg, RoundedCornerShape(999.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                Text(idVerificationStatusLabel(r.status, i18n), color = fg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(8.dp))
            Text(timeAgoT(r.createdAt), color = TextMuted, fontSize = 11.sp)
        }

        Spacer(Modifier.height(10.dp))
        Text(t("admin.boost_requests_listing"), color = TextMuted, fontSize = 11.sp)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onOpenListing(r.listingId) }) {
            Icon(Icons.Filled.Link, contentDescription = null, tint = Primary, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(r.listing?.title ?: "?", color = Primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(8.dp))
        Text(t("admin.boost_requests_seller"), color = TextMuted, fontSize = 11.sp)
        Text("${r.user?.name ?: "?"} · ${r.user?.email ?: ""}", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)

        Spacer(Modifier.height(10.dp))
        Text(
            "${t("admin.boost_requests_tiers")}: ${r.tiers.joinToString(", ") { i18n.t("boost.tier_${it}_name") }}  ·  ${t("admin.boost_requests_price")}: ${priceParts.first} ${priceParts.second}",
            color = TextPrimary, fontSize = 13.sp, lineHeight = 19.sp
        )

        r.adminNote?.takeIf { it.isNotBlank() }?.let {
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Filled.Edit, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                Text(it, color = TextMuted, fontSize = 12.sp, lineHeight = 17.sp)
            }
        }

        if (r.status == "PENDING") {
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onApprove, colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                    Text(t("admin.boost_requests_approve"))
                }
                OutlinedButton(onClick = onReject) {
                    Text(t("admin.boost_requests_reject"))
                }
            }
        }
    }
}

@Composable
private fun BoostRequestEmptyState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.RocketLaunch, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
        Spacer(Modifier.height(12.dp))
        Text(t("admin.boost_requests_empty"), color = TextMuted, textAlign = TextAlign.Center)
    }
}
