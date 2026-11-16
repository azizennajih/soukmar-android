package com.soukmar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.soukmar.app.data.remote.dto.ListingDto
import com.soukmar.app.ui.i18n.LocalFavorites
import com.soukmar.app.ui.i18n.cityLabelT
import com.soukmar.app.ui.i18n.formatPricePartsT
import com.soukmar.app.ui.i18n.t
import com.soukmar.app.ui.i18n.timeAgoT
import com.soukmar.app.ui.model.CategoryIcon
import com.soukmar.app.ui.model.HIGHLIGHT_ATTR_CODES
import com.soukmar.app.ui.model.countryFlag
import com.soukmar.app.ui.model.isBoostActive
import com.soukmar.app.ui.model.isNewListing
import com.soukmar.app.ui.theme.Accent
import com.soukmar.app.ui.theme.BorderColor
import com.soukmar.app.ui.theme.Gold
import com.soukmar.app.ui.theme.GoldLight
import com.soukmar.app.ui.theme.Primary
import com.soukmar.app.ui.theme.ShapeCard
import com.soukmar.app.ui.theme.Spacing
import com.soukmar.app.ui.theme.SuccessColor
import com.soukmar.app.ui.theme.SurfaceMuted
import com.soukmar.app.ui.theme.TextMuted
import com.soukmar.app.ui.theme.TextPrimary
import com.soukmar.app.ui.theme.TextSubtle
import com.soukmar.app.ui.theme.WhiteColor

private fun highlightFor(listing: ListingDto): String? {
    val codes = HIGHLIGHT_ATTR_CODES[listing.category] ?: return null
    for (code in codes) {
        val av = listing.attributeValues.find { it.attributeDefinition?.code == code } ?: continue
        val def = av.attributeDefinition ?: continue
        if (def.type == "BOOLEAN") continue
        val display = when (def.type) {
            "NUMBER" -> av.valueNumber?.let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() }
            else -> av.valueText
        }
        if (!display.isNullOrBlank()) return display
    }
    return null
}

/**
 * The listing card of the whole app (home, search results, favourites, seller profile, similar listings).
 * Square photo, price first, then title, place and time, seller. Yellow marks paid visibility (Top, Spotlight, Premium);
 * the heart sits on the photo and has a 44-dp touch target. [showFavorite] is off where the screen has its own
 * favourite control.
 */
@Composable
fun ListingCard(
    listing: ListingDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showFavorite: Boolean = true,
) {
    val priceParts = listing.price?.let { formatPricePartsT(it, listing.currency) }
    val isBoostTop = isBoostActive(listing.boostTopUntil)
    val isBoostSpotlight = !isBoostTop && isBoostActive(listing.boostSpotlightUntil)
    val favorites = LocalFavorites.current

    Column(
        modifier = modifier
            .clip(ShapeCard)
            .background(if (isBoostSpotlight) GoldLight else WhiteColor)
            .border(if (isBoostTop) 1.5.dp else 1.dp, if (isBoostTop) Accent else BorderColor, ShapeCard)
            .clickable(role = Role.Button, onClick = onClick)
    ) {
        Box {
            if (listing.images.isNotEmpty()) {
                AsyncImage(
                    model = listing.images.first(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(SurfaceMuted),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(SurfaceMuted),
                    contentAlignment = Alignment.Center
                ) {
                    CategoryIcon(category = listing.category, tint = TextSubtle, modifier = Modifier.size(40.dp))
                }
            }

            Column(
                modifier = Modifier.align(Alignment.TopStart).padding(Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                when {
                    isBoostTop -> SoukBadge(t("listing.boost_top_badge"), container = Accent, content = TextPrimary, icon = Icons.Outlined.WorkspacePremium)
                    isBoostSpotlight -> SoukBadge(t("listing.boost_spotlight_badge"), container = Accent, content = TextPrimary, icon = Icons.Outlined.Bolt)
                    listing.isFeatured -> SoukBadge(t("listing.premium_badge"), container = Accent, content = TextPrimary, icon = Icons.Outlined.Star)
                    listing.isPremium -> SoukBadge(t("listing.pro_badge"), container = Accent, content = TextPrimary)
                }
                if (listing.status != "RESERVED" && isNewListing(listing.createdAt)) {
                    SoukBadge(t("listing.new_badge"), container = WhiteColor, content = Primary)
                }
            }

            if (showFavorite && favorites != null) {
                val isFavorite = listing.id in favorites.ids
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable(
                            role = Role.Button,
                            onClickLabel = t(if (isFavorite) "listing.fav_remove" else "listing.fav_add")
                        ) { favorites.toggle(listing.id) },
                    contentAlignment = Alignment.Center
                ) {
                    Box(Modifier.size(32.dp).background(WhiteColor.copy(alpha = 0.92f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(
                            if (isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = t(if (isFavorite) "listing.fav_remove" else "listing.fav_add"),
                            tint = if (isFavorite) Primary else TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Column(modifier = Modifier.padding(Spacing.md)) {
            if (priceParts != null) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(priceParts.first, color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, maxLines = 1)
                    Spacer(Modifier.width(4.dp))
                    Text(priceParts.second, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, maxLines = 1, modifier = Modifier.padding(bottom = 1.dp))
                }
            } else {
                Text(t("listing.negotiate"), color = Gold, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(2.dp))
            Text(
                listing.title,
                color = TextPrimary,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            highlightFor(listing)?.let {
                Spacer(Modifier.height(2.dp))
                Text(it, color = TextMuted, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(Spacing.sm))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(2.dp))
                Text(
                    cityLabelT(listing.city), color = TextMuted, style = MaterialTheme.typography.bodySmall,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f)
                )
                // Only shown for non-Morocco listings — Morocco is the
                // overwhelming majority, so a foreign listing is the one
                // worth calling out while browsing. Mirrors web's foreignFlag.
                if (listing.country != "MA") {
                    Text(countryFlag(listing.country), fontSize = 12.sp)
                    Spacer(Modifier.width(4.dp))
                }
                Spacer(Modifier.width(4.dp))
                Text(timeAgoT(listing.createdAt), color = TextSubtle, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            }
            listing.user?.let { seller ->
                Spacer(Modifier.height(Spacing.xs))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (seller.accountType == "BUSINESS") Icons.Outlined.Business else Icons.Outlined.Person,
                        contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        seller.name, color = TextMuted, style = MaterialTheme.typography.bodySmall,
                        maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false)
                    )
                    if (seller.emailVerified || seller.phoneVerified || seller.idVerified) {
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Outlined.Verified, contentDescription = null, tint = SuccessColor, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}
