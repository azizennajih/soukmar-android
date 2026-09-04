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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.soukmar.app.data.remote.dto.AdminReportDto
import com.soukmar.app.ui.model.timeAgo
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

private fun statusLabel(status: String): String = when (status) {
    "PENDING" -> "En attente"
    "RESOLVED" -> "Résolu"
    "DISMISSED" -> "Rejeté"
    else -> "Toutes"
}

@Composable
fun AdminScreen(
    onBack: () -> Unit,
    onOpenListing: (String) -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Signalements") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour") } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FILTERS.forEach { f ->
                    FilterChip(
                        selected = viewModel.filter == f,
                        onClick = { viewModel.filter = f },
                        label = { Text("${statusLabel(f)} (${viewModel.countFor(f)})") },
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
    }

    viewModel.actionTarget?.let {
        val status = viewModel.actionStatus ?: return@let
        AlertDialog(
            onDismissRequest = { viewModel.cancelAction() },
            title = { Text(if (status == "RESOLVED") "Résoudre le signalement" else "Rejeter le signalement") },
            text = {
                Column {
                    Text("Note interne (optionnel)", color = TextMuted, fontSize = 12.sp)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = viewModel.actionNote,
                        onValueChange = { viewModel.actionNote = it },
                        placeholder = { Text("Note interne (optionnel)") },
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
                TextButton(onClick = { viewModel.cancelAction() }) { Text("Annuler") }
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
                Text(statusLabel(report.status), color = fg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(8.dp))
            Text(timeAgo(report.createdAt), color = TextMuted, fontSize = 11.sp)
        }

        Spacer(Modifier.height(10.dp))
        Text("Signalé par", color = TextMuted, fontSize = 11.sp)
        Text("${report.reporter?.name ?: "?"} · ${report.reporter?.email ?: ""}", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)

        Spacer(Modifier.height(8.dp))
        Text("Utilisateur signalé", color = TextMuted, fontSize = 11.sp)
        Text("${report.reported?.name ?: "?"} · ${report.reported?.email ?: ""}", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)

        report.listing?.let { listing ->
            Spacer(Modifier.height(8.dp))
            Text(
                "📌 ${listing.title}",
                color = Primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onOpenListing(listing.id) }
            )
        }

        Spacer(Modifier.height(10.dp))
        Text(report.reason, color = TextPrimary, fontSize = 14.sp, lineHeight = 20.sp)

        report.adminNote?.takeIf { it.isNotBlank() }?.let {
            Spacer(Modifier.height(8.dp))
            Text("📝 $it", color = TextMuted, fontSize = 12.sp, lineHeight = 17.sp)
        }

        if (report.status == "PENDING") {
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onResolve, colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                    Text("✅ Résoudre")
                }
                OutlinedButton(onClick = onDismiss) {
                    Text("❌ Rejeter")
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
        Text("🚩", fontSize = 40.sp)
        Spacer(Modifier.height(12.dp))
        Text("Aucun signalement pour le moment.", color = TextMuted, textAlign = TextAlign.Center)
    }
}
