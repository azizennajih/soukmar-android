@file:OptIn(ExperimentalMaterial3Api::class)

package com.soukmar.app.ui.screens.listings

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.List as ListIcon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.soukmar.app.ui.components.ListingCard
import com.soukmar.app.ui.components.ListingsMapView
import com.soukmar.app.ui.components.TextAutocompleteField
import com.soukmar.app.ui.i18n.t
import com.soukmar.app.ui.i18n.tCatalog
import com.soukmar.app.ui.model.CATEGORIES
import com.soukmar.app.ui.model.CategoryIcon
import com.soukmar.app.ui.model.JOB_PROFESSIONS_BY_SECTOR
import com.soukmar.app.ui.model.JOB_PROFESSION_CODES
import com.soukmar.app.ui.theme.Primary
import com.soukmar.app.ui.theme.PrimaryLight
import com.soukmar.app.ui.theme.TextMuted
import com.soukmar.app.ui.theme.TextPrimary
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

@Composable
fun ListingsScreen(
    initialCategory: String?,
    savedSearchId: String?,
    editSearchId: String? = null,
    onBack: () -> Unit,
    onOpenListing: (String) -> Unit,
    viewModel: ListingsViewModel = hiltViewModel()
) {
    var showFilters by remember { mutableStateOf(false) }
    var showMapView by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (editSearchId != null) {
            viewModel.applySavedSearchForEdit(editSearchId)
        } else if (savedSearchId != null) {
            viewModel.applySavedSearchById(savedSearchId)
        } else if (initialCategory != null && viewModel.selectedCategory == null) {
            viewModel.setCategory(initialCategory)
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        OutlinedTextField(
                            value = viewModel.query,
                            onValueChange = { viewModel.query = it },
                            placeholder = { Text(t("nav.search_placeholder")) },
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
                        IconButton(onClick = { showMapView = !showMapView }) {
                            Icon(
                                if (showMapView) Icons.AutoMirrored.Filled.ListIcon else Icons.Filled.Map,
                                contentDescription = t(if (showMapView) "annonces.view_list" else "annonces.view_map"),
                                tint = if (showMapView) Primary else TextPrimary
                            )
                        }
                        IconButton(onClick = { showFilters = !showFilters }) { Icon(Icons.Filled.FilterList, contentDescription = t("annonces.filters"), tint = if (showFilters) Primary else TextPrimary) }
                    }
                )
                CategoryChipsRow(selected = viewModel.selectedCategory, onSelect = { viewModel.setCategory(it) })
                if (viewModel.isLoggedIn) SaveSearchSection(viewModel)
                if (showFilters) FiltersPanel(viewModel)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                viewModel.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
                viewModel.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(viewModel.error ?: t("common.error"), color = TextMuted)
                }
                viewModel.listings.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(t("annonces.empty"), color = TextMuted)
                }
                showMapView -> {
                    ListingsMapView(
                        listings = viewModel.listings,
                        modifier = Modifier.fillMaxSize(),
                        onMarkerClick = onOpenListing
                    )
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
            FilterChipItem(label = t("nav.all"), selected = selected == null, onClick = { onSelect(null) })
        }
        items(CATEGORIES.size) { i ->
            val c = CATEGORIES[i]
            FilterChipItem(
                label = tCatalog("cats.${c.value}", c.value),
                icon = { CategoryIcon(c.value, tint = if (selected == c.value) Primary else TextMuted, modifier = Modifier.size(15.dp)) },
                selected = selected == c.value,
                onClick = { onSelect(c.value) }
            )
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
private fun FilterChipItem(label: String, icon: (@Composable () -> Unit)? = null, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = icon,
        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryLight, selectedLabelColor = Primary)
    )
}

@Composable
private fun SaveSearchSection(viewModel: ListingsViewModel) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
        when {
            viewModel.searchSaved -> Text("✅ ${t("annonces.search_saved")}", color = com.soukmar.app.ui.theme.SuccessColor, style = MaterialTheme.typography.labelMedium)
            viewModel.showSaveSearchForm -> {
                if (viewModel.editSearchId != null) {
                    Text(t("annonces.edit_search_title"), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(Modifier.height(4.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = viewModel.newSearchName,
                        onValueChange = { viewModel.newSearchName = it },
                        placeholder = { Text(t("annonces.save_search_name")) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, cursorColor = Primary)
                    )
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = { viewModel.cancelSaveSearch() }) { Text(t("common.cancel")) }
                    Button(
                        onClick = { viewModel.saveSearch() },
                        enabled = viewModel.newSearchName.isNotBlank() && !viewModel.savingSearch,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text(if (viewModel.savingSearch) "…" else if (viewModel.editSearchId != null) t("annonces.update_search") else t("common.save"))
                    }
                }
                viewModel.saveSearchError?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(it, color = com.soukmar.app.ui.theme.ErrorColor, style = MaterialTheme.typography.labelSmall)
                }
            }
            else -> TextButton(onClick = { viewModel.showSaveSearchForm = true }) {
                Text("🔔 ${t("annonces.save_search")}")
            }
        }
    }
}

/** Requests the runtime location permission if needed, then a one-shot
 * current location via `FusedLocationProviderClient` — mirrors web's
 * `navigator.geolocation.getCurrentPosition()` (`GeocodeService.
 * getCurrentPosition()`). Returns a trigger function; the actual permission
 * prompt/GPS fetch happens asynchronously and reports back through
 * [ListingsViewModel]'s own `lat`/`lng`/`locationLoading`/`locationError`.
 *
 * Uses the plain platform `LocationManager` rather than Play Services'
 * `FusedLocationProviderClient` — the latter depends on Play Services' own
 * network location backend (Google account + live connectivity to Google's
 * servers), which a plain GPS-only fix can't satisfy and which real budget
 * devices without a signed-in Google account can't rely on either;
 * `LocationManager` talks to GPS directly and works everywhere. */
@Composable
private fun rememberLocationRequester(viewModel: ListingsViewModel): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    suspend fun LocationManager.awaitSingleLocation(provider: String): Location? {
        if (!isProviderEnabled(provider)) return null
        return suspendCancellableCoroutine { cont ->
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    if (cont.isActive) cont.resume(location, onCancellation = null)
                }
            }
            requestSingleUpdate(provider, listener, Looper.getMainLooper())
            cont.invokeOnCancellation { removeUpdates(listener) }
        }
    }

    fun fetchLocation() {
        viewModel.updateLocationLoading(true)
        scope.launch {
            try {
                val locationManager = ContextCompat.getSystemService(context, LocationManager::class.java)
                val location = locationManager?.let {
                    withTimeoutOrNull(15_000) {
                        it.awaitSingleLocation(LocationManager.GPS_PROVIDER)
                            ?: it.awaitSingleLocation(LocationManager.NETWORK_PROVIDER)
                    }
                }
                if (location != null) {
                    viewModel.setLocation(location.latitude, location.longitude)
                } else {
                    viewModel.updateLocationError("annonces.gps_error")
                }
            } catch (e: SecurityException) {
                viewModel.updateLocationError("annonces.gps_error_denied")
            } catch (e: Exception) {
                viewModel.updateLocationError("annonces.gps_error")
            }
            viewModel.updateLocationLoading(false)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) fetchLocation() else viewModel.updateLocationError("annonces.gps_error_denied")
    }

    return {
        viewModel.updateLocationError(null)
        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            fetchLocation()
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }
}

@Composable
private fun FiltersPanel(viewModel: ListingsViewModel) {
    // FiltersPanel lives inside Scaffold's topBar, which doesn't scroll — a
    // category with many EAV attributes (e.g. Véhicules) can otherwise push
    // "Appliquer"/"Réinitialiser" off the bottom of the screen with no way
    // to reach them. Bound the height and scroll internally instead.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 420.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        val requestLocation = rememberLocationRequester(viewModel)

        Text(t("annonces.city"), style = MaterialTheme.typography.labelMedium, color = TextMuted)
        Spacer(Modifier.height(6.dp))
        if (viewModel.lat != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.MyLocation, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(t("annonces.current_location"), color = TextPrimary, modifier = Modifier.weight(1f))
                IconButton(onClick = { viewModel.clearLocation() }) {
                    Icon(Icons.Filled.Close, contentDescription = t("common.cancel"), tint = TextMuted)
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(t("annonces.radius"), style = MaterialTheme.typography.labelMedium, color = TextMuted)
            Spacer(Modifier.height(4.dp))
            LazyRowChips {
                items(listOf("5", "10", "20", "30", "50", "100", "150", "200")) { r ->
                    FilterChipItem(label = "+$r km", selected = viewModel.radius == r, onClick = { viewModel.selectRadius(r) })
                }
            }
        } else {
            OutlinedButton(onClick = { requestLocation() }, enabled = !viewModel.locationLoading) {
                if (viewModel.locationLoading) {
                    CircularProgressIndicator(color = Primary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Icon(Icons.Filled.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(t("annonces.use_gps"))
            }
            viewModel.locationError?.let {
                Spacer(Modifier.height(4.dp))
                Text(t(it), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(10.dp))

        Text(t("annonces.sort"), style = MaterialTheme.typography.labelMedium, color = TextMuted)
        Spacer(Modifier.height(6.dp))
        LazyRowChips {
            item { FilterChipItem(label = t("annonces.newest"), selected = viewModel.sort == "default", onClick = { viewModel.selectSort("default") }) }
            item { FilterChipItem(label = t("annonces.price_asc"), selected = viewModel.sort == "prix_asc", onClick = { viewModel.selectSort("prix_asc") }) }
            item { FilterChipItem(label = t("annonces.price_desc"), selected = viewModel.sort == "prix_desc", onClick = { viewModel.selectSort("prix_desc") }) }
            if (viewModel.lat != null) {
                item { FilterChipItem(label = t("annonces.distance"), selected = viewModel.sort == "distance", onClick = { viewModel.selectSort("distance") }) }
            }
        }
        Spacer(Modifier.height(10.dp))

        if (viewModel.subcategories.isNotEmpty()) {
            Text(t("annonces.subcategory"), style = MaterialTheme.typography.labelMedium, color = TextMuted)
            Spacer(Modifier.height(6.dp))
            LazyRowChips {
                items(viewModel.subcategories.size) { i ->
                    val sub = viewModel.subcategories[i]
                    FilterChipItem(label = tCatalog("subcats.${sub.code}", sub.code), selected = viewModel.selectedSubcategoryId == sub.id, onClick = { viewModel.setSubcategory(sub.id) })
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        if (viewModel.showCondition) {
            Text(t("annonces.condition"), style = MaterialTheme.typography.labelMedium, color = TextMuted)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChipItem(label = t("listing.condition_new"), selected = viewModel.selectedCondition == "NEW", onClick = { viewModel.setCondition("NEW") })
                FilterChipItem(label = t("listing.condition_used"), selected = viewModel.selectedCondition == "USED", onClick = { viewModel.setCondition("USED") })
            }
            Spacer(Modifier.height(10.dp))
        }

        Text(t("annonces.price"), style = MaterialTheme.typography.labelMedium, color = TextMuted)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = viewModel.minPrice, onValueChange = { viewModel.minPrice = it },
                placeholder = { Text(t("annonces.min")) }, singleLine = true, modifier = Modifier.weight(1f),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
            )
            OutlinedTextField(
                value = viewModel.maxPrice, onValueChange = { viewModel.maxPrice = it },
                placeholder = { Text(t("annonces.max")) }, singleLine = true, modifier = Modifier.weight(1f),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
            )
        }
        Spacer(Modifier.height(8.dp))

        Text(t("auth.account_type"), style = MaterialTheme.typography.labelMedium, color = TextMuted)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChipItem(label = t("auth.account_type_private"), selected = viewModel.selectedAccountType == "PRIVATE", onClick = { viewModel.setAccountType("PRIVATE") })
            FilterChipItem(label = t("auth.account_type_business"), selected = viewModel.selectedAccountType == "BUSINESS", onClick = { viewModel.setAccountType("BUSINESS") })
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
                Text(t("annonces.apply"))
            }
            OutlinedButton(onClick = { viewModel.clearFilters() }) { Text(t("annonces.reset")) }
        }
    }
}

@Composable
private fun AttributeFilter(def: com.soukmar.app.data.remote.dto.AttributeDefinitionDto, viewModel: ListingsViewModel) {
    val label = tCatalog("attrs.${def.code}", def.code)
    Text(label, style = MaterialTheme.typography.labelMedium, color = TextMuted)
    Spacer(Modifier.height(6.dp))
    if (def.code == "PROFESSION") {
        val sectors = viewModel.attrSelections["INDUSTRY"].orEmpty()
        val options = if (sectors.isEmpty()) JOB_PROFESSION_CODES else sectors.flatMap { JOB_PROFESSIONS_BY_SECTOR[it].orEmpty() }.distinct()
        TextAutocompleteField(
            value = viewModel.attrTextFilters[def.code] ?: "",
            onValueChange = { viewModel.setAttrText(def.code, it) },
            options = options,
            labelPrefix = "job_professions."
        )
        return
    }
    when (def.type) {
        "SELECT" -> {
            LazyRowChips {
                items(def.options.size) { i ->
                    val opt = def.options[i]
                    val selected = viewModel.attrSelections[def.code]?.contains(opt) == true
                    FilterChipItem(label = tCatalog("attrs.opts.$opt", opt), selected = selected, onClick = { viewModel.toggleAttrOption(def.code, opt) })
                }
            }
        }
        "BOOLEAN" -> {
            val selected = viewModel.attrSelections[def.code]?.contains("true") == true
            FilterChipItem(label = t("common.yes"), selected = selected, onClick = { viewModel.toggleAttrOption(def.code, "true") })
        }
        "NUMBER" -> {
            val current = viewModel.attrRanges[def.code] ?: ("" to "")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = current.first,
                    onValueChange = { viewModel.setAttrRange(def.code, it, current.second) },
                    placeholder = { Text(t("annonces.min")) }, singleLine = true, modifier = Modifier.weight(1f),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
                OutlinedTextField(
                    value = current.second,
                    onValueChange = { viewModel.setAttrRange(def.code, current.first, it) },
                    placeholder = { Text(t("annonces.max")) }, singleLine = true, modifier = Modifier.weight(1f),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
            }
        }
        else -> { /* free-text attributes aren't filterable in the web app either */ }
    }
}
