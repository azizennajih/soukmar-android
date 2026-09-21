@file:OptIn(ExperimentalMaterial3Api::class)

package com.soukmar.app.ui.screens.imagesearch

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.soukmar.app.ui.components.ListingCard
import com.soukmar.app.ui.i18n.t
import com.soukmar.app.ui.theme.BorderColor
import com.soukmar.app.ui.theme.ErrorColor
import com.soukmar.app.ui.theme.Primary
import com.soukmar.app.ui.theme.PrimaryLight
import com.soukmar.app.ui.theme.TextMuted
import com.soukmar.app.ui.theme.TextPrimary
import com.soukmar.app.ui.theme.WhiteColor

/** "Search by photo" — mirrors soukmar's `image-search.component.html`:
 * a dropzone-style photo picker, a loading state, then either a grid of
 * [ListingCard] results (closest visual match first) or an empty state.
 * Android has no drag&drop, so the "dropzone" is just a tappable picker
 * card — same pattern as Phase 4's photo picker. */
@Composable
fun ImageSearchScreen(
    onBack: () -> Unit,
    onOpenListing: (String) -> Unit,
    onBrowseAll: () -> Unit,
    viewModel: ImageSearchViewModel = hiltViewModel()
) {
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.runSearch(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(t("image_search.title")) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour") } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(t("image_search.subtitle"), color = TextMuted, fontSize = 13.sp)
                Spacer(Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (viewModel.previewUri != null) WhiteColor else PrimaryLight)
                        .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
                        .clickable { picker.launch("image/*") },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (viewModel.previewUri != null) {
                        AsyncImage(
                            model = viewModel.previewUri,
                            contentDescription = t("image_search.title"),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp))
                        )
                    } else {
                        Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = Primary, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(t("image_search.pick_btn"), color = Primary, fontWeight = FontWeight.SemiBold)
                    }
                }

                if (viewModel.previewUri != null) {
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(onClick = { picker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                        Text(t("image_search.pick_btn"))
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when {
                    viewModel.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Primary)
                            Spacer(Modifier.height(10.dp))
                            Text(t("image_search.loading"), color = TextMuted, fontSize = 13.sp)
                        }
                    }
                    viewModel.errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(viewModel.errorMessage!!, color = ErrorColor, textAlign = TextAlign.Center, modifier = Modifier.padding(24.dp))
                    }
                    viewModel.searched && viewModel.results.isEmpty() -> EmptyResultsState(onBrowseAll)
                    viewModel.results.isNotEmpty() -> Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            "${t("image_search.results_count")} (${viewModel.results.size})",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(viewModel.results, key = { it.id }) { listing ->
                                ListingCard(listing = listing, onClick = { onOpenListing(listing.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyResultsState(onBrowseAll: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.Image, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
        Spacer(Modifier.height(12.dp))
        Text(t("image_search.no_results"), color = TextPrimary, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text(t("image_search.no_results_sub"), color = TextMuted, fontSize = 12.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onBrowseAll, colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
            Text(t("mes_favoris.browse_btn"))
        }
    }
}
