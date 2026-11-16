@file:OptIn(ExperimentalMaterial3Api::class)

package com.soukmar.app.ui.screens.home

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.soukmar.app.ui.components.CountrySwitcher
import com.soukmar.app.ui.components.ListingCard
import com.soukmar.app.ui.components.SearchPill
import com.soukmar.app.ui.components.SectionHeader
import com.soukmar.app.ui.components.SoukMarLogo
import com.soukmar.app.ui.components.SponsorBanner
import com.soukmar.app.ui.i18n.t
import com.soukmar.app.ui.i18n.tCatalog
import com.soukmar.app.ui.model.CATEGORIES
import com.soukmar.app.ui.model.CategoryIcon
import com.soukmar.app.ui.model.categoryConfig
import com.soukmar.app.ui.theme.Accent
import com.soukmar.app.ui.theme.IconSize
import com.soukmar.app.ui.theme.Primary
import com.soukmar.app.ui.theme.ShapeCard
import com.soukmar.app.ui.theme.Spacing
import com.soukmar.app.ui.theme.SurfaceMuted
import com.soukmar.app.ui.theme.TextMuted
import com.soukmar.app.ui.theme.TextPrimary
import com.soukmar.app.ui.theme.WhiteColor

/** Home: logo and header actions, the large search, categories at a glance, premium and newest listings. Bottom bar comes from the nav graph. */
@Composable
fun HomeScreen(
    onOpenCategory: (String) -> Unit,
    onOpenAllCategories: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenImageSearch: () -> Unit,
    onOpenFavoris: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenListing: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
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
        containerColor = WhiteColor,
        topBar = {
            TopAppBar(
                title = { SoukMarLogo() },
                actions = {
                    IconButton(onClick = onOpenFavoris) {
                        Icon(Icons.Outlined.FavoriteBorder, contentDescription = t("nav.my_favorites"), modifier = Modifier.size(IconSize.standard))
                    }
                    IconButton(onClick = onOpenNotifications) {
                        BadgedBox(badge = {
                            if (viewModel.unreadNotifications > 0) {
                                Badge(containerColor = Primary, contentColor = WhiteColor) {
                                    Text(if (viewModel.unreadNotifications > 99) "99+" else viewModel.unreadNotifications.toString())
                                }
                            }
                        }) {
                            Icon(Icons.Outlined.NotificationsNone, contentDescription = t("nav.notifications"), modifier = Modifier.size(IconSize.standard))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WhiteColor, scrolledContainerColor = WhiteColor)
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = Spacing.lg, end = Spacing.lg, bottom = Spacing.xl),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }, key = "search") {
                SearchPill(
                    placeholder = t("nav.search_placeholder"),
                    onClick = onOpenSearch,
                    onCameraClick = onOpenImageSearch,
                    cameraLabel = t("nav.search_by_image")
                )
            }

            // Categories at a glance: one horizontal row, the full grid is the second tab.
            item(span = { GridItemSpan(maxLineSpan) }, key = "categories") {
                Column {
                    SectionHeader(t("nav.categories"), actionLabel = t("home.see_all"), onAction = onOpenAllCategories)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                        items(CATEGORIES, key = { it.value }) { cat -> CategoryChip(cat.value, onClick = { onOpenCategory(cat.value) }) }
                    }
                }
            }

            if (viewModel.interests.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }, key = "interests") {
                    Column {
                        SectionHeader(t("home.welcome_back"))
                        Text(t("home.your_interests"), color = TextMuted, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(Spacing.sm))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            items(viewModel.interests, key = { it.category }) { interest ->
                                Row(
                                    modifier = Modifier
                                        .background(SurfaceMuted, CircleShape)
                                        .clickable(role = Role.Button) { onOpenCategory(interest.category) }
                                        .heightIn(min = 48.dp)
                                        .padding(horizontal = Spacing.md),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CategoryIcon(interest.category, tint = TextPrimary, modifier = Modifier.size(IconSize.small))
                                    Spacer(Modifier.width(Spacing.sm))
                                    Column {
                                        Text(tCatalog("cats.${interest.category}", interest.category), style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                                        Text(t("home.new_listings_count", "count" to interest.newListingsCount.toString()), style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            viewModel.inlineBanners.firstOrNull()?.let { banner ->
                item(span = { GridItemSpan(maxLineSpan) }, key = "sponsor") {
                    SponsorBanner(banner, onClick = { viewModel.bannerClicked(banner.id) })
                }
            }

            if (viewModel.featuredListings.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }, key = "featured_header") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(8.dp).background(Accent, CircleShape))
                        Spacer(Modifier.width(Spacing.sm))
                        SectionHeader(t("home.featured"), Modifier.weight(1f))
                    }
                }
                items(viewModel.featuredListings, key = { "f_" + it.id }) { listing ->
                    ListingCard(listing = listing, onClick = { onOpenListing(listing.id) })
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }, key = "recent_header") {
                SectionHeader(t("home.recent"), actionLabel = t("home.see_all"), onAction = onOpenSearch)
            }
            if (viewModel.feedLoading && viewModel.recentListings.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }, key = "loading") {
                    Box(Modifier.fillMaxWidth().padding(Spacing.xl), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
                }
            }
            items(viewModel.recentListings, key = { "r_" + it.id }) { listing ->
                ListingCard(listing = listing, onClick = { onOpenListing(listing.id) })
            }

            // The country lives at the very bottom (like the web footer), not in the top bar.
            item(span = { GridItemSpan(maxLineSpan) }, key = "country") {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.lg), contentAlignment = Alignment.Center) {
                    CountrySwitcher(country = viewModel.country, onSelect = { viewModel.selectCountry(it) })
                }
            }
        }
    }
}

/** Round icon with the category name below; 72 dp wide, so the whole chip is a comfortable touch target. */
@Composable
fun CategoryChip(category: String, onClick: () -> Unit) {
    val cat = categoryConfig(category)
    Column(
        modifier = Modifier.width(76.dp).clickable(role = Role.Button, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.size(60.dp).background(cat?.bg ?: SurfaceMuted, CircleShape), contentAlignment = Alignment.Center) {
            CategoryIcon(category, tint = cat?.fg ?: TextPrimary, modifier = Modifier.size(IconSize.feature))
        }
        Spacer(Modifier.height(6.dp))
        Text(
            tCatalog("cats.$category", category),
            style = MaterialTheme.typography.labelMedium,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
