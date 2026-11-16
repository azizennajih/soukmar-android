package com.soukmar.app.ui.screens.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soukmar.app.data.country.CountryRepository
import com.soukmar.app.data.remote.ApiService
import com.soukmar.app.ui.components.SoukTopBar
import com.soukmar.app.ui.i18n.t
import com.soukmar.app.ui.i18n.tCatalog
import com.soukmar.app.ui.model.CATEGORY_GROUPS
import com.soukmar.app.ui.model.CategoryIcon
import com.soukmar.app.ui.model.categoryConfig
import com.soukmar.app.ui.theme.BorderColor
import com.soukmar.app.ui.theme.IconSize
import com.soukmar.app.ui.theme.Spacing
import com.soukmar.app.ui.theme.TextMuted
import com.soukmar.app.ui.theme.TextPrimary
import com.soukmar.app.ui.theme.TextSubtle
import com.soukmar.app.ui.theme.WhiteColor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val api: ApiService,
    private val countryRepository: CountryRepository
) : ViewModel() {
    var counts by mutableStateOf<Map<String, Int>>(emptyMap())
        private set

    init {
        viewModelScope.launch {
            try {
                val res = api.getCategoryCounts(countryRepository.country)
                counts = if (res.isSuccessful) res.body() ?: emptyMap() else emptyMap()
            } catch (_: Exception) {
                counts = emptyMap()
            }
        }
    }
}

/** All categories in the four groups of the web home page, one tidy list: icon, name, number of listings. */
@Composable
fun CategoriesScreen(onOpenCategory: (String) -> Unit, viewModel: CategoriesViewModel = hiltViewModel()) {
    Scaffold(containerColor = WhiteColor, topBar = { SoukTopBar(title = t("nav.categories")) }) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = Spacing.xl)) {
            CATEGORY_GROUPS.forEach { (groupKey, values) ->
                item(key = "g_$groupKey") {
                    Text(
                        t("home.cat_group_$groupKey"),
                        style = MaterialTheme.typography.labelLarge,
                        color = TextMuted,
                        modifier = Modifier.padding(start = Spacing.lg, end = Spacing.lg, top = Spacing.lg, bottom = Spacing.xs)
                    )
                }
                items(values, key = { it }) { value ->
                    val cat = categoryConfig(value)
                    val count = viewModel.counts[value] ?: 0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(role = Role.Button) { onOpenCategory(value) }
                            .heightIn(min = 64.dp)
                            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(44.dp).background(cat?.bg ?: BorderColor, CircleShape), contentAlignment = Alignment.Center) {
                            CategoryIcon(value, tint = cat?.fg ?: TextPrimary, modifier = Modifier.size(IconSize.standard))
                        }
                        Spacer(Modifier.width(Spacing.lg))
                        Column(Modifier.weight(1f)) {
                            Text(tCatalog("cats.$value", value), style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                            if (count > 0) {
                                Text(
                                    if (count == 1) t("home.category_count_one") else t("home.category_count", "count" to count.toString()),
                                    style = MaterialTheme.typography.bodySmall, color = TextMuted
                                )
                            }
                        }
                        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = TextSubtle)
                    }
                }
            }
        }
    }
}
