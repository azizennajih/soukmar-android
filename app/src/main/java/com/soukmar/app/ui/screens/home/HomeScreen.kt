@file:OptIn(ExperimentalMaterial3Api::class)

package com.soukmar.app.ui.screens.home

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.soukmar.app.ui.components.CountrySwitcher
import com.soukmar.app.ui.components.LanguageSwitcher
import com.soukmar.app.ui.components.SoukMarLogo
import com.soukmar.app.ui.i18n.t
import com.soukmar.app.ui.i18n.tCatalog
import com.soukmar.app.ui.model.CATEGORIES
import com.soukmar.app.ui.model.CategoryIcon
import com.soukmar.app.ui.model.categoryConfig
import com.soukmar.app.ui.theme.BorderColor
import com.soukmar.app.ui.theme.Primary
import com.soukmar.app.ui.theme.TextMuted
import com.soukmar.app.ui.theme.TextPrimary
import com.soukmar.app.ui.theme.WhiteColor

@Composable
fun HomeScreen(
    onOpenCategory: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenDeposerAnnonce: () -> Unit,
    onOpenChat: () -> Unit,
    onOpenMesAnnonces: () -> Unit,
    onOpenFavoris: () -> Unit,
    onOpenSavedSearches: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenAdmin: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { SoukMarLogo() },
                actions = {
                    CountrySwitcher(country = viewModel.country, onSelect = { viewModel.selectCountry(it) })
                    LanguageSwitcher()
                    IconButton(onClick = onOpenFavoris) {
                        Icon(Icons.Filled.FavoriteBorder, contentDescription = t("nav.my_favorites"))
                    }
                    IconButton(onClick = onOpenMesAnnonces) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = t("nav.my_listings"))
                    }
                    IconButton(onClick = onOpenChat) {
                        Icon(Icons.Filled.ChatBubbleOutline, contentDescription = t("nav.messages"))
                    }
                    IconButton(onClick = onOpenNotifications) {
                        BadgedBox(badge = {
                            if (viewModel.unreadNotifications > 0) {
                                Badge { Text(if (viewModel.unreadNotifications > 99) "99+" else viewModel.unreadNotifications.toString()) }
                            }
                        }) {
                            Icon(Icons.Filled.NotificationsNone, contentDescription = t("nav.notifications"))
                        }
                    }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = null)
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            if (viewModel.user?.role == "ADMIN") {
                                DropdownMenuItem(
                                    text = { Text(t("nav.admin")) },
                                    leadingIcon = { Icon(Icons.Filled.Shield, contentDescription = null) },
                                    onClick = { menuExpanded = false; onOpenAdmin() }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text(t("nav.saved_searches")) },
                                leadingIcon = { Icon(Icons.Filled.BookmarkBorder, contentDescription = null) },
                                onClick = { menuExpanded = false; onOpenSavedSearches() }
                            )
                            DropdownMenuItem(
                                text = { Text(t("nav.settings")) },
                                leadingIcon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                                onClick = { menuExpanded = false; onOpenSettings() }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onOpenDeposerAnnonce,
                containerColor = Primary,
                contentColor = WhiteColor,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(t("nav.post_full")) }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            viewModel.user?.let { u ->
                Text("Bonjour, ${u.name} 👋", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(Modifier.height(12.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenSearch)
                    .background(WhiteColor, RoundedCornerShape(999.dp))
                    .border(1.dp, BorderColor, RoundedCornerShape(999.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Search, contentDescription = null, tint = TextMuted)
                Spacer(Modifier.width(8.dp))
                Text(t("nav.search_placeholder"), color = TextMuted)
            }

            if (viewModel.interests.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                InterestsSection(viewModel.interests, onOpenCategory)
            }

            Spacer(Modifier.height(20.dp))
            Text(t("nav.categories"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(10.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(CATEGORIES, key = { it.value }) { cat ->
                    Column(
                        modifier = Modifier
                            .clickable { onOpenCategory(cat.value) }
                            .background(cat.bg, RoundedCornerShape(14.dp))
                            .padding(vertical = 16.dp, horizontal = 8.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CategoryIcon(cat.value, tint = cat.fg, modifier = Modifier.size(26.dp))
                        Spacer(Modifier.height(6.dp))
                        Text(tCatalog("cats.${cat.value}", cat.value), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = cat.fg, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            }
        }
    }
}

/** Mirrors the web home page's personalized "Vos centres d'intérêt actuels"
 * section — top 3 recency-weighted categories from GET /listings/interests,
 * only shown when there's at least one (a fresh account or one with no
 * recent activity just won't see the section, same as the web). */
@Composable
private fun InterestsSection(interests: List<com.soukmar.app.data.remote.dto.InterestDto>, onOpenCategory: (String) -> Unit) {
    Column {
        Text("👋 ${t("home.welcome_back")}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text(t("home.your_interests"), color = TextMuted, fontSize = 12.sp)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            interests.forEach { interest ->
                val cat = categoryConfig(interest.category)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenCategory(interest.category) }
                        .background(cat?.bg ?: WhiteColor, RoundedCornerShape(14.dp))
                        .padding(vertical = 14.dp, horizontal = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (cat != null) {
                        CategoryIcon(cat.value, tint = cat.fg, modifier = Modifier.size(22.dp))
                    } else {
                        Text("🏷️", fontSize = 22.sp)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        tCatalog("cats.${interest.category}", interest.category),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = cat?.fg ?: TextPrimary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        t("home.new_listings_count", "count" to interest.newListingsCount.toString()),
                        fontSize = 9.sp,
                        color = (cat?.fg ?: TextMuted).copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
