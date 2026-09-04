@file:OptIn(ExperimentalMaterial3Api::class)

package com.soukmar.app.ui.screens.savedsearches

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.soukmar.app.data.remote.dto.SavedSearchDto
import com.soukmar.app.ui.model.categoryConfig
import com.soukmar.app.ui.theme.BorderColor
import com.soukmar.app.ui.theme.Primary
import com.soukmar.app.ui.theme.TextMuted
import com.soukmar.app.ui.theme.TextPrimary
import com.soukmar.app.ui.theme.WhiteColor
import com.soukmar.app.ui.i18n.t
import com.soukmar.app.ui.i18n.tCatalog

@Composable
fun SavedSearchesScreen(
    onBack: () -> Unit,
    onOpenSearch: (String) -> Unit,
    viewModel: SavedSearchesViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${t("saved_searches.title")} (${viewModel.searches.size})") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour") } }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                viewModel.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
                viewModel.searches.isEmpty() -> EmptyState()
                else -> LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(viewModel.searches, key = { it.id }) { search ->
                        SavedSearchRow(
                            search = search,
                            onOpen = { onOpenSearch(search.id) },
                            onDelete = { viewModel.remove(search.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedSearchRow(search: SavedSearchDto, onOpen: () -> Unit, onDelete: () -> Unit) {
    val cat = search.category?.let { categoryConfig(it) }
    val catLabel = cat?.let { tCatalog("cats.${it.value}", it.value) }
    val madLabel = t("common.mad")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .background(WhiteColor, RoundedCornerShape(14.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(search.name, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.height(4.dp))
            val meta = buildList {
                if (cat != null && catLabel != null) add("${cat.emoji} $catLabel")
                search.city?.let { add("📍 $it") }
                if (search.minPrice != null || search.maxPrice != null) {
                    val min = search.minPrice?.let { formatPlain(it) } ?: "0"
                    val max = search.maxPrice?.let { formatPlain(it) } ?: "∞"
                    add("$min–$max $madLabel")
                }
            }
            if (meta.isNotEmpty()) {
                Text(meta.joinToString(" · "), color = TextMuted, fontSize = 12.sp)
            }
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Close, contentDescription = t("common.delete"), tint = TextMuted)
        }
    }
}

private fun formatPlain(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🔔", fontSize = 40.sp)
        Spacer(Modifier.height(12.dp))
        Text(t("saved_searches.empty"), style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        Spacer(Modifier.height(4.dp))
        Text(
            t("saved_searches.empty_sub"),
            color = TextMuted,
            textAlign = TextAlign.Center
        )
    }
}
