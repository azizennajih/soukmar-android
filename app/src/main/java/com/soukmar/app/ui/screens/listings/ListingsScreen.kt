@file:OptIn(ExperimentalMaterial3Api::class)

package com.soukmar.app.ui.screens.listings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.soukmar.app.ui.components.ListingCard
import com.soukmar.app.ui.model.CATEGORIES
import com.soukmar.app.ui.model.CONDITION_CATEGORIES
import com.soukmar.app.ui.theme.Primary
import com.soukmar.app.ui.theme.PrimaryLight
import com.soukmar.app.ui.theme.TextMuted
import com.soukmar.app.ui.theme.TextPrimary

@Composable
fun ListingsScreen(
    initialCategory: String?,
    onBack: () -> Unit,
    onOpenListing: (String) -> Unit,
    viewModel: ListingsViewModel = hiltViewModel()
) {
    var showFilters by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (initialCategory != null && viewModel.selectedCategory == null) viewModel.setCategory(initialCategory)
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        OutlinedTextField(
                            value = viewModel.query,
                            onValueChange = { viewModel.query = it },
                            placeholder = { Text("Rechercher sur SoukMar…") },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                            shape = RoundedCornerShape(999.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, cursorColor = Primary),
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { viewModel.search() }),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Retour") }
                    },
                    actions = {
                        IconButton(onClick = { showFilters = !showFilters }) { Icon(Icons.Filled.FilterList, contentDescription = "Filtres", tint = if (showFilters) Primary else TextPrimary) }
                    }
                )
                CategoryChipsRow(selected = viewModel.selectedCategory, onSelect = { viewModel.setCategory(it) })
                if (showFilters) FiltersPanel(viewModel)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                viewModel.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
                viewModel.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(viewModel.error ?: "Erreur", color = TextMuted)
                }
                viewModel.listings.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Aucune annonce trouvée.", color = TextMuted)
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(viewModel.listings, key = { it.id }) { listing ->
                            ListingCard(listing = listing, onClick = { onOpenListing(listing.id) })
                        }
                        if (viewModel.hasMore) {
                            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                                Box(Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                                    if (viewModel.loadingMore) {
                                        CircularProgressIndicator(color = Primary, modifier = Modifier.size(28.dp))
                                    } else {
                                        OutlinedButton(onClick = { viewModel.loadMore() }) { Text("Charger plus") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChipsRow(selected: String?, onSelect: (String?) -> Unit) {
    LazyRowChips {
        item {
            FilterChipItem(label = "Tout", emoji = "🏷️", selected = selected == null, onClick = { onSelect(null) })
        }
        items(CATEGORIES.size) { i ->
            val c = CATEGORIES[i]
            FilterChipItem(label = c.label, emoji = c.emoji, selected = selected == c.value, onClick = { onSelect(c.value) })
        }
    }
}

@Composable
private fun LazyRowChips(content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit) {
    androidx.compose.foundation.lazy.LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

@Composable
private fun FilterChipItem(label: String, emoji: String? = null, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(if (emoji != null) "$emoji $label" else label) },
        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryLight, selectedLabelColor = Primary)
    )
}

@Composable
private fun FiltersPanel(viewModel: ListingsViewModel) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
        if (viewModel.subcategories.isNotEmpty()) {
            Text("Sous-catégorie", style = MaterialTheme.typography.labelMedium, color = TextMuted)
            Spacer(Modifier.height(6.dp))
            LazyRowChips {
                items(viewModel.subcategories.size) { i ->
                    val sub = viewModel.subcategories[i]
                    FilterChipItem(label = sub.code.lowercase().replaceFirstChar { it.uppercase() }, selected = viewModel.selectedSubcategoryId == sub.id, onClick = { viewModel.setSubcategory(sub.id) })
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        if (viewModel.selectedCategory != null && CONDITION_CATEGORIES.contains(viewModel.selectedCategory)) {
            Text("État", style = MaterialTheme.typography.labelMedium, color = TextMuted)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChipItem(label = "Neuf", selected = viewModel.selectedCondition == "NEW", onClick = { viewModel.setCondition("NEW") })
                FilterChipItem(label = "Occasion", selected = viewModel.selectedCondition == "USED", onClick = { viewModel.setCondition("USED") })
            }
            Spacer(Modifier.height(10.dp))
        }

        Text("Prix (MAD)", style = MaterialTheme.typography.labelMedium, color = TextMuted)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = viewModel.minPrice, onValueChange = { viewModel.minPrice = it },
                placeholder = { Text("Min") }, singleLine = true, modifier = Modifier.weight(1f),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
            )
            OutlinedTextField(
                value = viewModel.maxPrice, onValueChange = { viewModel.maxPrice = it },
                placeholder = { Text("Max") }, singleLine = true, modifier = Modifier.weight(1f),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
            )
        }
        Spacer(Modifier.height(8.dp))

        if (viewModel.filterableAttributes.isNotEmpty()) {
            for (def in viewModel.filterableAttributes) {
                Spacer(Modifier.height(8.dp))
                AttributeFilter(def, viewModel)
            }
        }

        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { viewModel.search() }, colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                Text("Appliquer")
            }
            OutlinedButton(onClick = { viewModel.clearFilters() }) { Text("Réinitialiser") }
        }
    }
}

@Composable
private fun AttributeFilter(def: com.soukmar.app.data.remote.dto.AttributeDefinitionDto, viewModel: ListingsViewModel) {
    val label = def.code.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }
    Text(label, style = MaterialTheme.typography.labelMedium, color = TextMuted)
    Spacer(Modifier.height(6.dp))
    when (def.type) {
        "SELECT" -> {
            LazyRowChips {
                items(def.options.size) { i ->
                    val opt = def.options[i]
                    val selected = viewModel.attrSelections[def.code]?.contains(opt) == true
                    FilterChipItem(label = opt.lowercase().replaceFirstChar { it.uppercase() }, selected = selected, onClick = { viewModel.toggleAttrOption(def.code, opt) })
                }
            }
        }
        "BOOLEAN" -> {
            val selected = viewModel.attrSelections[def.code]?.contains("true") == true
            FilterChipItem(label = "Oui", selected = selected, onClick = { viewModel.toggleAttrOption(def.code, "true") })
        }
        "NUMBER" -> {
            val current = viewModel.attrRanges[def.code] ?: ("" to "")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = current.first,
                    onValueChange = { viewModel.setAttrRange(def.code, it, current.second) },
                    placeholder = { Text("Min") }, singleLine = true, modifier = Modifier.weight(1f),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
                OutlinedTextField(
                    value = current.second,
                    onValueChange = { viewModel.setAttrRange(def.code, current.first, it) },
                    placeholder = { Text("Max") }, singleLine = true, modifier = Modifier.weight(1f),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
            }
        }
        else -> { /* free-text attributes aren't filterable in the web app either */ }
    }
}
