package com.soukmar.app.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.soukmar.app.data.remote.dto.SponsorBannerDto
import com.soukmar.app.ui.i18n.t
import com.soukmar.app.ui.theme.BorderColor
import com.soukmar.app.ui.theme.TextMuted

/** One advertising banner with the mandatory "Werbung" label (mirrors the web's app-sponsor-banner).
 * A tap opens the link in the browser; [onClick] reports the click for the aggregate counter. */
@Composable
fun SponsorBanner(banner: SponsorBannerDto, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            .clickable {
                onClick()
                // Only https links are stored by the backend; anything else is not opened.
                if (banner.linkUrl.startsWith("https://")) {
                    try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(banner.linkUrl))) } catch (e: Exception) { /* no browser */ }
                }
            }
    ) {
        Text(t("sponsor.label"), fontSize = 10.sp, color = TextMuted, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
        AsyncImage(
            model = banner.imageUrl,
            contentDescription = banner.altText.ifBlank { t("sponsor.label") },
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
