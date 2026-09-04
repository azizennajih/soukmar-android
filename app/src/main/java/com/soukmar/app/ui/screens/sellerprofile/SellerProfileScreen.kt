@file:OptIn(ExperimentalMaterial3Api::class)

package com.soukmar.app.ui.screens.sellerprofile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.soukmar.app.data.remote.dto.ReviewWithDetailsDto
import com.soukmar.app.ui.components.ListingCard
import com.soukmar.app.ui.i18n.timeAgoT
import com.soukmar.app.ui.theme.BorderColor
import com.soukmar.app.ui.theme.Gold
import com.soukmar.app.ui.theme.Primary
import com.soukmar.app.ui.theme.TextMuted
import com.soukmar.app.ui.theme.TextPrimary
import com.soukmar.app.ui.theme.WhiteColor
import com.soukmar.app.ui.i18n.t
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun SellerProfileScreen(
    sellerId: String,
    onBack: () -> Unit,
    onOpenListing: (String) -> Unit,
    viewModel: SellerProfileViewModel = hiltViewModel()
) {
    LaunchedEffect(sellerId) { viewModel.load(sellerId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil vendeur") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour") } }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                viewModel.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
                viewModel.notFound || viewModel.profile == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(t("seller.not_found"), color = TextMuted)
                }
                else -> SellerProfileContent(viewModel, onOpenListing)
            }
        }
    }
}

@Composable
private fun SellerProfileContent(viewModel: SellerProfileViewModel, onOpenListing: (String) -> Unit) {
    val profile = viewModel.profile!!
    val listingRows = viewModel.listings.chunked(2)

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth().background(WhiteColor, RoundedCornerShape(14.dp)).border(1.dp, BorderColor, RoundedCornerShape(14.dp)).padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(84.dp).clip(CircleShape).background(Primary),
                    contentAlignment = Alignment.Center
                ) {
                    if (profile.image != null) {
                        AsyncImage(model = profile.image, contentDescription = profile.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else {
                        Text(profile.name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 32.sp)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(profile.name, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimary)
                profile.city?.let {
                    Spacer(Modifier.height(2.dp))
                    Text("📍 $it", color = TextMuted, fontSize = 13.sp)
                }
                memberSince(profile.createdAt)?.let {
                    Spacer(Modifier.height(4.dp))
                    Text("${t("seller.member_since")} $it", color = TextMuted, fontSize = 12.sp)
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (profile.avgRating != null && profile.avgRating > 0) {
                        StarRow(rating = profile.avgRating.roundToInt())
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "%.1f (%d)".format(profile.avgRating, profile.reviewCount),
                            color = TextMuted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Text(t("seller.no_reviews"), color = TextMuted, fontSize = 13.sp)
                    }
                }
                responseLabel(profile.avgResponseHours)?.let {
                    Spacer(Modifier.height(8.dp))
                    Box(modifier = Modifier.background(Color(0xFFF1F5F9), RoundedCornerShape(999.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                        Text("⚡ $it", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(6.dp))
            Text("${t("seller.listings_title")} (${profile.activeListingsCount})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        if (listingRows.isEmpty()) {
            item { Text(t("seller.no_listings"), color = TextMuted, fontSize = 13.sp) }
        } else {
            items(listingRows) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { listing ->
                        ListingCard(listing = listing, onClick = { onOpenListing(listing.id) }, modifier = Modifier.weight(1f))
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }

        item {
            Spacer(Modifier.height(6.dp))
            Text("${t("seller.reviews_title")} (${viewModel.reviews.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        if (viewModel.reviews.isEmpty()) {
            item { Text(t("seller.no_reviews_yet"), color = TextMuted, fontSize = 13.sp) }
        } else {
            items(viewModel.reviews, key = { it.id }) { ReviewRow(it) }
        }
    }
}

@Composable
private fun StarRow(rating: Int) {
    Row {
        (1..5).forEach { s ->
            Text("★", fontSize = 15.sp, color = if (s <= rating) Gold else BorderColor)
        }
    }
}

@Composable
private fun ReviewRow(review: ReviewWithDetailsDto) {
    Column(
        modifier = Modifier.fillMaxWidth().background(WhiteColor, RoundedCornerShape(14.dp)).border(1.dp, BorderColor, RoundedCornerShape(14.dp)).padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(Primary),
                contentAlignment = Alignment.Center
            ) {
                Text(review.reviewer?.name?.take(1)?.uppercase() ?: "?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(review.reviewer?.name ?: "Anonyme", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                StarRow(rating = review.rating)
            }
        }
        review.comment?.takeIf { it.isNotBlank() }?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = TextPrimary, fontSize = 13.sp, lineHeight = 18.sp)
        }
        Spacer(Modifier.height(6.dp))
        val listingTitle = review.listing?.title
        Text(
            if (listingTitle != null) "📌 $listingTitle · ${timeAgoT(review.createdAt)}" else timeAgoT(review.createdAt),
            color = TextMuted,
            fontSize = 11.sp
        )
    }
}

private val memberSinceFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH).withZone(ZoneId.systemDefault())

private fun memberSince(iso: String?): String? {
    if (iso == null) return null
    return try {
        memberSinceFormatter.format(Instant.parse(iso))
    } catch (e: DateTimeParseException) {
        null
    }
}

/** Mirrors the web's seller.responds_minutes/_hours/_days copy thresholds. */
@Composable
private fun responseLabel(hours: Double?): String? {
    if (hours == null) return null
    return when {
        hours < 1 -> t("seller.responds_minutes")
        hours < 24 -> t("seller.responds_hours", "hours" to hours.roundToInt().toString())
        else -> t("seller.responds_days", "days" to (hours / 24).roundToInt().toString())
    }
}
