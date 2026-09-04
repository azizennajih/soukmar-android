@file:OptIn(ExperimentalMaterial3Api::class)

package com.soukmar.app.ui.screens.mesannonces

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.soukmar.app.data.remote.dto.ListingDto
import com.soukmar.app.ui.model.categoryConfig
import com.soukmar.app.ui.model.formatPriceParts
import com.soukmar.app.ui.model.timeAgo
import com.soukmar.app.ui.theme.BorderColor
import com.soukmar.app.ui.theme.ErrorColor
import com.soukmar.app.ui.theme.Primary
import com.soukmar.app.ui.theme.PrimaryLight
import com.soukmar.app.ui.theme.TextMuted
import com.soukmar.app.ui.theme.TextPrimary
import com.soukmar.app.ui.theme.WhiteColor

private data class StatusStyle(val label: String, val bg: Color, val fg: Color)

private fun statusStyle(status: String): StatusStyle = when (status) {
    "ACTIVE" -> StatusStyle("Active", Color(0xFFDCFCE7), Color(0xFF15803D))
    "RESERVED" -> StatusStyle("Réservée", Color(0xFFFEF9C3), Color(0xFFA16207))
    "PENDING" -> StatusStyle("En attente", Color(0xFFFEF9C3), Color(0xFFA16207))
    "SOLD" -> StatusStyle("Vendue", Color(0xFFDBEAFE), Color(0xFF1D4ED8))
    "REJECTED" -> StatusStyle("Rejetée", Color(0xFFFEE2E2), Color(0xFFB91C1C))
    "EXPIRED" -> StatusStyle("Expirée", Color(0xFFFEE2E2), Color(0xFFB91C1C))
    else -> StatusStyle(status, BorderColor, TextMuted)
}

@Composable
fun MesAnnoncesScreen(
    onBack: () -> Unit,
    onOpenListing: (String) -> Unit,
    onEditListing: (String) -> Unit,
    onNewListing: () -> Unit,
    viewModel: MesAnnoncesViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(viewModel.toastMessage) {
        viewModel.toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearToast()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Mes annonces (${viewModel.listings.size})") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour") } },
                actions = {
                    IconButton(onClick = onNewListing) { Icon(Icons.Filled.Add, contentDescription = "Nouvelle annonce") }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                viewModel.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
                viewModel.listings.isEmpty() -> EmptyMesAnnonces(onNewListing)
                else -> LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(viewModel.listings, key = { it.id }) { listing ->
                        Column {
                            ListingRow(
                                listing = listing,
                                canBump = viewModel.canBump(listing),
                                bumping = viewModel.bumpingId == listing.id,
                                statsOpen = viewModel.statsOpenId == listing.id,
                                onOpen = { onOpenListing(listing.id) },
                                onEdit = { onEditListing(listing.id) },
                                onToggleReserve = { viewModel.toggleReserve(listing) },
                                onBump = { viewModel.bump(listing) },
                                onToggleStats = { viewModel.toggleStats(listing) },
                                onDelete = { viewModel.requestDelete(listing.id) }
                            )
                            if (viewModel.statsOpenId == listing.id) {
                                StatsPanel(viewModel.statsData[listing.id])
                            }
                        }
                    }
                }
            }
        }
    }

    if (viewModel.deleteConfirmId != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDelete() },
            title = { Text("Supprimer cette annonce ?") },
            text = { Text("Cette action est irréversible.") },
            confirmButton = { TextButton(onClick = { viewModel.confirmDelete() }) { Text("Supprimer", color = ErrorColor) } },
            dismissButton = { TextButton(onClick = { viewModel.dismissDelete() }) { Text("Annuler") } }
        )
    }
}

@Composable
private fun EmptyMesAnnonces(onNewListing: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("📋", fontSize = 40.sp)
        Spacer(Modifier.height(12.dp))
        Text("Aucune annonce", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        Spacer(Modifier.height(4.dp))
        Text("Vous n'avez pas encore publié d'annonce.", color = TextMuted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onNewListing, colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
            Text("Déposer une annonce")
        }
    }
}

@Composable
private fun ListingRow(
    listing: ListingDto,
    canBump: Boolean,
    bumping: Boolean,
    statsOpen: Boolean,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onToggleReserve: () -> Unit,
    onBump: () -> Unit,
    onToggleStats: () -> Unit,
    onDelete: () -> Unit
) {
    val cat = categoryConfig(listing.category)
    val priceParts = listing.price?.let { formatPriceParts(it, listing.currency) }
    val style = statusStyle(listing.status)
    val canToggleReserve = listing.status == "ACTIVE" || listing.status == "RESERVED"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(WhiteColor, RoundedCornerShape(14.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Row {
            Box(
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(10.dp)).clickable(onClick = onOpen),
            ) {
                if (listing.images.isNotEmpty()) {
                    AsyncImage(model = listing.images.first(), contentDescription = listing.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(cat?.bg ?: BorderColor), contentAlignment = Alignment.Center) {
                        Text(cat?.emoji ?: "📦", fontSize = 24.sp)
                    }
                }
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f).clickable(onClick = onOpen)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(listing.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(6.dp))
                    Box(modifier = Modifier.background(style.bg, RoundedCornerShape(999.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                        Text(style.label, color = style.fg, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    if (priceParts != null) "${priceParts.first} ${priceParts.second}" else "À négocier",
                    color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp
                )
                Spacer(Modifier.height(2.dp))
                Text("👁 ${listing.views} vues · 🕐 ${timeAgo(listing.createdAt)} · 📍 ${listing.city}", color = TextMuted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            RowActionButton(Icons.Filled.Visibility, "Voir", onClick = onOpen)
            if (canToggleReserve) {
                RowActionButton(
                    if (listing.status == "RESERVED") Icons.Filled.LockOpen else Icons.Filled.Lock,
                    if (listing.status == "RESERVED") "Remettre en vente" else "Réserver",
                    active = listing.status == "RESERVED",
                    onClick = onToggleReserve
                )
            }
            RowActionButton(Icons.Filled.Edit, "Modifier", onClick = onEdit)
            if (canToggleReserve) {
                RowActionButton(Icons.Filled.ArrowUpward, if (canBump) "Remonter" else "Déjà remontée aujourd'hui", enabled = canBump && !bumping, onClick = onBump)
            }
            RowActionButton(Icons.Filled.BarChart, "Statistiques", active = statsOpen, onClick = onToggleStats)
            RowActionButton(Icons.Filled.Delete, "Supprimer", danger = true, onClick = onDelete)
        }
    }
}

@Composable
private fun RowActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    enabled: Boolean = true,
    active: Boolean = false,
    danger: Boolean = false,
    onClick: () -> Unit
) {
    val tint = when {
        !enabled -> TextMuted.copy(alpha = 0.4f)
        danger -> ErrorColor
        active -> Primary
        else -> TextMuted
    }
    IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(36.dp)) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun StatsPanel(days: List<com.soukmar.app.data.remote.dto.ViewStatDayDto>?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PrimaryLight, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text("Vues des 14 derniers jours", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        if (days == null) {
            Box(Modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Primary, strokeWidth = 2.dp)
            }
        } else {
            val maxCount = (days.maxOfOrNull { it.count } ?: 0).coerceAtLeast(1)
            Row(
                modifier = Modifier.fillMaxWidth().height(48.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                days.forEach { d ->
                    val heightFraction = (d.count.toFloat() / maxCount).coerceIn(0.06f, 1f)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(heightFraction)
                            .background(Primary, RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                    )
                }
            }
        }
    }
}
