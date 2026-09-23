@file:OptIn(ExperimentalMaterial3Api::class)

package com.soukmar.app.ui.screens.boostlisting

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.soukmar.app.data.remote.dto.BOOST_TIERS
import com.soukmar.app.data.remote.dto.BoostTier
import com.soukmar.app.data.remote.dto.BoostTierId
import com.soukmar.app.data.remote.dto.quoteBoostPrice
import com.soukmar.app.ui.components.ErrorBanner
import com.soukmar.app.ui.i18n.LocalI18n
import com.soukmar.app.ui.i18n.formatPricePartsT
import com.soukmar.app.ui.i18n.t
import com.soukmar.app.ui.theme.BorderColor
import com.soukmar.app.ui.theme.Primary
import com.soukmar.app.ui.theme.PrimaryLight
import com.soukmar.app.ui.theme.TextMuted
import com.soukmar.app.ui.theme.TextPrimary
import com.soukmar.app.ui.theme.WhiteColor

private fun iconFor(tier: BoostTierId): ImageVector = when (tier) {
    BoostTierId.bump -> Icons.Filled.ArrowUpward
    BoostTierId.spotlight -> Icons.Filled.Bolt
    BoostTierId.top -> Icons.Filled.WorkspacePremium
    BoostTierId.global -> Icons.Filled.Public
}

@Composable
fun BoostListingScreen(
    listingId: String,
    onBack: () -> Unit,
    viewModel: BoostListingViewModel = hiltViewModel()
) {
    LaunchedEffect(listingId) { viewModel.load(listingId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(t("boost.title")) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = t("boost.back")) } }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                viewModel.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
                viewModel.loadError || viewModel.listing == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(t("boost.error_generic"), color = TextMuted)
                }
                else -> BoostListingContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun BoostListingContent(viewModel: BoostListingViewModel) {
    val listing = viewModel.listing ?: return
    val pendingRequest = viewModel.boostStatus?.pendingRequest

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        // Listing summary
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(WhiteColor, RoundedCornerShape(14.dp))
                .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (listing.images.isNotEmpty()) {
                AsyncImage(
                    model = listing.images.first(),
                    contentDescription = listing.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(10.dp))
                )
                Spacer(Modifier.width(12.dp))
            }
            Column {
                Text(listing.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary, maxLines = 1)
                listing.price?.let {
                    val parts = formatPricePartsT(it, listing.currency)
                    Text("${parts.first} ${parts.second}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text(t("boost.title"), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        Spacer(Modifier.height(6.dp))
        Text(t("boost.subtitle"), color = TextMuted, fontSize = 13.sp, lineHeight = 19.sp)
        Spacer(Modifier.height(18.dp))

        when {
            pendingRequest != null && pendingRequest.status == "PENDING" -> {
                val i18n = LocalI18n.current
                val tierNames = pendingRequest.tiers.joinToString(", ") { i18n.t("boost.tier_" + it.lowercase() + "_name") }
                val priceParts = formatPricePartsT(pendingRequest.totalPrice, pendingRequest.currency)
                PendingOrConfirmBanner(
                    icon = Icons.Filled.HourglassEmpty,
                    title = t("boost.pending_title"),
                    body = t("boost.pending_body", "tiers" to tierNames, "price" to "${priceParts.first} ${priceParts.second}")
                )
            }
            viewModel.submitted -> {
                PendingOrConfirmBanner(icon = Icons.Filled.CheckCircle, title = t("boost.confirm_title"), body = t("boost.confirm_body"))
            }
            else -> {
                BOOST_TIERS.forEach { tier ->
                    TierCard(
                        tier = tier,
                        selected = tier.id in viewModel.selectedTiers,
                        activeUntil = when (tier.id) {
                            BoostTierId.spotlight -> viewModel.boostStatus?.boostSpotlightUntil
                            BoostTierId.top -> viewModel.boostStatus?.boostTopUntil
                            BoostTierId.global -> viewModel.boostStatus?.boostGlobalUntil
                            BoostTierId.bump -> null
                        }?.takeIf { viewModel.isActiveUntil(it) },
                        onToggle = { viewModel.toggle(tier.id) }
                    )
                    Spacer(Modifier.height(10.dp))
                }

                Spacer(Modifier.height(8.dp))
                SummaryCard(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun TierCard(tier: BoostTier, selected: Boolean, activeUntil: String?, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) PrimaryLight else WhiteColor)
            .border(if (selected) 1.5.dp else 1.dp, if (selected) Primary else BorderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onToggle)
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Checkbox(checked = selected, onCheckedChange = { onToggle() }, colors = CheckboxDefaults.colors(checkedColor = Primary))
        Spacer(Modifier.width(6.dp))
        Box(
            modifier = Modifier.size(36.dp).background(PrimaryLight, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(iconFor(tier.id), contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(t("boost.tier_${tier.id.name}_name"), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                Text("${tier.priceMAD} MAD", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
            }
            Spacer(Modifier.height(3.dp))
            Text(t("boost.tier_${tier.id.name}_desc"), color = TextMuted, fontSize = 12.sp, lineHeight = 17.sp)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(modifier = Modifier.background(BorderColor, RoundedCornerShape(999.dp)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                    Text(
                        if (tier.durationDays == null) t("boost.duration_instant") else t("boost.duration_days", "n" to tier.durationDays.toString()),
                        color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold
                    )
                }
                if (activeUntil != null) {
                    Box(modifier = Modifier.background(PrimaryLight, RoundedCornerShape(999.dp)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                        Text(t("boost.active_until", "date" to activeUntil.take(10)), color = Primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(viewModel: BoostListingViewModel) {
    val quote = quoteBoostPrice(viewModel.selectedTiers)
    var showSelectOneError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth().background(WhiteColor, RoundedCornerShape(14.dp)).border(1.dp, BorderColor, RoundedCornerShape(14.dp)).padding(16.dp)
    ) {
        SummaryRow(t("boost.subtotal"), "${quote.subtotal} MAD")
        if (quote.discountPercent > 0) {
            SummaryRow(t("boost.discount"), "-${quote.subtotal - quote.total} MAD", color = Primary)
        }
        Spacer(Modifier.height(6.dp))
        HorizontalDivider(color = BorderColor)
        Spacer(Modifier.height(6.dp))
        SummaryRow(t("boost.total"), "${quote.total} MAD", bold = true)

        Spacer(Modifier.height(10.dp))
        Text(t("boost.payment_note"), color = TextMuted, fontSize = 11.sp, lineHeight = 16.sp)

        if (showSelectOneError && viewModel.selectedTiers.isEmpty()) {
            Spacer(Modifier.height(8.dp))
            ErrorBanner(t("boost.select_at_least_one"))
        }
        viewModel.errorMessage?.let {
            Spacer(Modifier.height(8.dp))
            ErrorBanner(it)
        }

        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { viewModel.submit(onSelectAtLeastOne = { showSelectOneError = true }) },
            enabled = !viewModel.submitting,
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text(if (viewModel.submitting) t("boost.submitting") else t("boost.submit"))
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, bold: Boolean = false, color: androidx.compose.ui.graphics.Color = TextMuted) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = if (bold) TextPrimary else color, fontSize = if (bold) 16.sp else 13.sp, fontWeight = if (bold) FontWeight.ExtraBold else FontWeight.Normal)
        Text(value, color = if (bold) TextPrimary else color, fontSize = if (bold) 16.sp else 13.sp, fontWeight = if (bold) FontWeight.ExtraBold else FontWeight.SemiBold)
    }
}

@Composable
private fun PendingOrConfirmBanner(icon: ImageVector, title: String, body: String) {
    Row(
        modifier = Modifier.fillMaxWidth().background(PrimaryLight, RoundedCornerShape(14.dp)).padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text(body, color = TextMuted, fontSize = 13.sp, lineHeight = 18.sp)
        }
    }
}
