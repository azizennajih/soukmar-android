package com.soukmar.app.ui.screens.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.soukmar.app.ui.components.MenuRow
import com.soukmar.app.ui.components.SoukTopBar
import com.soukmar.app.ui.components.VerifiedBadge
import com.soukmar.app.ui.components.soukCard
import com.soukmar.app.ui.i18n.t
import com.soukmar.app.ui.screens.home.HomeViewModel
import com.soukmar.app.ui.theme.BorderColor
import com.soukmar.app.ui.theme.Primary
import com.soukmar.app.ui.theme.Spacing
import com.soukmar.app.ui.theme.TextMuted
import com.soukmar.app.ui.theme.TextPrimary
import com.soukmar.app.ui.theme.WhiteColor

/** "My account" tab: who is signed in, then everything that used to hide in the overflow menu and the top bar, as one clear list. */
@Composable
fun AccountScreen(
    onOpenProfil: () -> Unit,
    onOpenMesAnnonces: () -> Unit,
    onOpenFavoris: () -> Unit,
    onOpenMesAbonnements: () -> Unit,
    onOpenSavedSearches: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenAdmin: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val user = viewModel.user
    Scaffold(containerColor = WhiteColor, topBar = { SoukTopBar(title = t("nav.account"), actions = { com.soukmar.app.ui.components.LanguageSwitcher() }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            if (user != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().soukCard().clickable(onClick = onOpenProfil).padding(Spacing.lg),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(56.dp).clip(CircleShape).background(Primary), contentAlignment = Alignment.Center) {
                        if (!user.image.isNullOrBlank()) {
                            AsyncImage(model = user.image, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        } else {
                            Text(user.name.take(1).uppercase(), color = WhiteColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        }
                    }
                    Spacer(Modifier.width(Spacing.lg))
                    Column(Modifier.weight(1f)) {
                        Text(user.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(user.email, style = MaterialTheme.typography.bodySmall, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        VerifiedBadge(user.emailVerified, user.phoneVerified, user.idVerified, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }

            Column(Modifier.fillMaxWidth().soukCard()) {
                MenuRow(Icons.AutoMirrored.Outlined.ListAlt, t("nav.my_listings"), onOpenMesAnnonces)
                HorizontalDivider(color = BorderColor)
                MenuRow(Icons.Outlined.FavoriteBorder, t("nav.my_favorites"), onOpenFavoris)
                HorizontalDivider(color = BorderColor)
                MenuRow(Icons.Outlined.BookmarkBorder, t("nav.saved_searches"), onOpenSavedSearches)
                HorizontalDivider(color = BorderColor)
                MenuRow(Icons.Outlined.Group, t("nav.my_follows"), onOpenMesAbonnements)
                HorizontalDivider(color = BorderColor)
                MenuRow(Icons.Outlined.NotificationsNone, t("nav.notifications"), onOpenNotifications)
            }

            Column(Modifier.fillMaxWidth().soukCard()) {
                MenuRow(Icons.Outlined.Settings, t("nav.settings"), onOpenSettings)
                if (user?.role == "ADMIN") {
                    HorizontalDivider(color = BorderColor)
                    MenuRow(Icons.Outlined.Shield, t("nav.admin"), onOpenAdmin)
                }
            }
            Spacer(Modifier.height(Spacing.lg))
        }
    }
}
