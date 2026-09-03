package com.soukmar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
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
import com.soukmar.app.ui.model.HIGHLIGHT_ATTR_CODES
import com.soukmar.app.ui.model.categoryConfig
import com.soukmar.app.ui.model.formatPriceParts
import com.soukmar.app.ui.model.timeAgo
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
    val priceParts = listing.price?.let { formatPriceParts(it, listing.currency) }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(WhiteColor)
            .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
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
            if (listing.isFeatured || listing.isPremium) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(GoldLight, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(if (listing.isFeatured) "⭐ Vedette" else "Premium", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                Text("À négocier", color = TextMuted, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
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
                Text(listing.city, color = TextMuted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(4.dp))
                Text(timeAgo(listing.createdAt), color = TextMuted, fontSize = 11.sp)
            }
            cat?.let {
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .background(it.bg, RoundedCornerShape(999.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text("${it.emoji} ${it.label}", color = it.fg, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
