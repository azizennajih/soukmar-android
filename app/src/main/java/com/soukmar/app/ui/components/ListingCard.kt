package com.soukmar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.soukmar.app.data.remote.dto.ListingDto
import com.soukmar.app.ui.i18n.cityLabelT
import com.soukmar.app.ui.i18n.formatPricePartsT
import com.soukmar.app.ui.i18n.t
import com.soukmar.app.ui.i18n.tCatalog
import com.soukmar.app.ui.i18n.timeAgoT
import com.soukmar.app.ui.model.CategoryIcon
import com.soukmar.app.ui.model.HIGHLIGHT_ATTR_CODES
import com.soukmar.app.ui.model.categoryConfig
import com.soukmar.app.ui.model.countryFlag
import com.soukmar.app.ui.model.isBoostActive
import com.soukmar.app.ui.model.isNewListing
import com.soukmar.app.ui.theme.Primary
import com.soukmar.app.ui.theme.PrimaryLight
import com.soukmar.app.ui.theme.BorderColor
import com.soukmar.app.ui.theme.Gold
import com.soukmar.app.ui.theme.GoldLight
import com.soukmar.app.ui.theme.TextMuted
import com.soukmar.app.ui.theme.TextPrimary
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

@Composable
fun ListingCard(listing: ListingDto, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val cat = categoryConfig(listing.category)
    val priceParts = listing.price?.let { formatPricePartsT(it, listing.currency) }
    val isBoostTop = isBoostActive(listing.boostTopUntil)
    val isBoostSpotlight = !isBoostTop && isBoostActive(listing.boostSpotlightUntil)
    val borderColor = if (isBoostTop) Primary else BorderColor
    val borderWidth = if (isBoostTop) 2.dp else 1.dp

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isBoostSpotlight) PrimaryLight else WhiteColor)
            .border(borderWidth, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Box {
            if (listing.images.isNotEmpty()) {
                AsyncImage(
                    model = listing.images.first(),
                    contentDescription = listing.title,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1.2f),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().aspectRatio(1.2f).background(Color(0xFFF1F5F9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.PhotoCamera, contentDescription = null, tint = TextMuted)
                }
            }
            if (isBoostTop || isBoostSpotlight) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(if (isBoostTop) Primary else PrimaryLight, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isBoostTop) Icons.Filled.WorkspacePremium else Icons.Filled.Bolt,
                            contentDescription = null,
                            tint = if (isBoostTop) WhiteColor else Primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            t(if (isBoostTop) "listing.boost_top_badge" else "listing.boost_spotlight_badge"),
                            color = if (isBoostTop) WhiteColor else Primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else if (listing.isFeatured || listing.isPremium) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(GoldLight, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    if (listing.isFeatured) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = Gold, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(3.dp))
                            Text(t("listing.premium_badge"), color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text("Pro", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            if (listing.status != "RESERVED" && isNewListing(listing.createdAt)) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(PrimaryLight, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(t("listing.new_badge"), color = Primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Column(modifier = Modifier.padding(10.dp)) {
            if (priceParts != null) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(priceParts.first, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.width(4.dp))
                    Text(priceParts.second, color = TextMuted, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                }
            } else {
                Text(t("listing.negotiate"), color = TextMuted, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                listing.title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            highlightFor(listing)?.let {
                Text(it, color = TextMuted, fontSize = 11.sp)
                Spacer(Modifier.height(4.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(2.dp))
                Text(cityLabelT(listing.city), color = TextMuted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                // Only shown for non-Morocco listings — Morocco is the
                // overwhelming majority, so a foreign listing is the one
                // worth calling out while browsing. Mirrors web's foreignFlag.
                if (listing.country != "MA") {
                    Text(countryFlag(listing.country), fontSize = 11.sp)
                    Spacer(Modifier.width(4.dp))
                }
                Spacer(Modifier.width(4.dp))
                Text(timeAgoT(listing.createdAt), color = TextMuted, fontSize = 11.sp)
            }
            cat?.let {
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .background(it.bg, RoundedCornerShape(999.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    CategoryIcon(category = it.value, tint = it.fg, modifier = Modifier.size(11.dp))
                    Text(tCatalog("cats.${it.value}", it.value), color = it.fg, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
