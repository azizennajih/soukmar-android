package com.soukmar.app.ui.screens.listings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soukmar.app.data.remote.dto.AttributeDefinitionDto
import com.soukmar.app.data.remote.dto.ListingDto
import com.soukmar.app.data.remote.dto.SubcategoryWithAttributesDto
import com.soukmar.app.data.repository.ApiResult
import com.soukmar.app.data.repository.CatalogRepository
import com.soukmar.app.data.repository.ListingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/** One selected value per filterable attribute code: a set of option codes
 * for SELECT/BOOLEAN, or a "min|max" pair for NUMBER — mirrors the web
 * annonces page's dynamic filter sidebar built from getCategoryFull(). */
@HiltViewModel
class ListingsViewModel @Inject constructor(
    private val listingRepository: ListingRepository,
    private val catalogRepository: CatalogRepository
) : ViewModel() {

    var query by mutableStateOf("")
    var selectedCategory by mutableStateOf<String?>(null)
        private set
    var selectedSubcategoryId by mutableStateOf<String?>(null)
        private set
    var selectedCondition by mutableStateOf<String?>(null)
        private set
    var minPrice by mutableStateOf("")
    var maxPrice by mutableStateOf("")
    var sort by mutableStateOf("default")

    var subcategories by mutableStateOf<List<SubcategoryWithAttributesDto>>(emptyList())
        private set
    var filterableAttributes by mutableStateOf<List<AttributeDefinitionDto>>(emptyList())
        private set

    // code -> selected option values (SELECT) or "true"/"false" (BOOLEAN)
    var attrSelections by mutableStateOf<Map<String, Set<String>>>(emptyMap())
        private set
    // code -> (min, max) raw text for NUMBER attributes
    var attrRanges by mutableStateOf<Map<String, Pair<String, String>>>(emptyMap())
        private set

    var listings by mutableStateOf<List<ListingDto>>(emptyList())
        private set
    var loading by mutableStateOf(false)
        private set
    var loadingMore by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    private var page = 1
    var hasMore by mutableStateOf(false)
        private set

    init {
        search()
    }

    fun setCategory(value: String?) {
        if (selectedCategory == value) return
        selectedCategory = value
        selectedSubcategoryId = null
        selectedCondition = null
        attrSelections = emptyMap()
        attrRanges = emptyMap()
        subcategories = emptyList()
        filterableAttributes = emptyList()
        if (value != null) loadCategoryFilters(value)
        search()
    }

    fun setSubcategory(id: String?) {
        selectedSubcategoryId = if (selectedSubcategoryId == id) null else id
        attrSelections = emptyMap()
        attrRanges = emptyMap()
        filterableAttributes = if (selectedSubcategoryId != null) {
            subcategories.find { it.id == selectedSubcategoryId }?.attributeDefinitions?.filter { it.filterable } ?: emptyList()
        } else {
            unionFilterableAttrs(subcategories)
        }
        search()
    }

    fun setCondition(value: String?) {
        selectedCondition = if (selectedCondition == value) null else value
        search()
    }

    fun toggleAttrOption(code: String, option: String) {
        val current = attrSelections[code] ?: emptySet()
        val updated = if (current.contains(option)) current - option else current + option
        attrSelections = attrSelections.toMutableMap().apply {
            if (updated.isEmpty()) remove(code) else put(code, updated)
        }
        search()
    }

    fun setAttrRange(code: String, min: String, max: String) {
        attrRanges = attrRanges.toMutableMap().apply { put(code, min to max) }
    }

    fun applyAttrRange() = search()

    fun clearFilters() {
        selectedSubcategoryId = null
        selectedCondition = null
        minPrice = ""
        maxPrice = ""
        attrSelections = emptyMap()
        attrRanges = emptyMap()
        filterableAttributes = unionFilterableAttrs(subcategories)
        search()
    }

    private fun unionFilterableAttrs(subs: List<SubcategoryWithAttributesDto>): List<AttributeDefinitionDto> {
        val seen = LinkedHashMap<String, AttributeDefinitionDto>()
        for (sub in subs) for (def in sub.attributeDefinitions) if (def.filterable && !seen.containsKey(def.code)) seen[def.code] = def
        return seen.values.toList()
    }

    private fun loadCategoryFilters(category: String) {
        viewModelScope.launch {
            when (val result = catalogRepository.getCategoryFull(category)) {
                is ApiResult.Success -> {
                    subcategories = result.data.subcategories
                    filterableAttributes = unionFilterableAttrs(result.data.subcategories)
                }
                is ApiResult.Error -> { /* filter sidebar is optional; browsing still works without it */ }
            }
        }
    }

    private fun buildParams(targetPage: Int): Map<String, String> {
        val params = mutableMapOf("page" to targetPage.toString(), "limit" to "20")
        if (query.isNotBlank()) params["q"] = query.trim()
        selectedCategory?.let { params["category"] = it }
        selectedSubcategoryId?.let { params["subcategoryId"] = it }
        selectedCondition?.let { params["condition"] = it }
        if (minPrice.isNotBlank()) params["minPrice"] = minPrice
        if (maxPrice.isNotBlank()) params["maxPrice"] = maxPrice
        if (sort != "default") params["tri"] = sort
        for ((code, values) in attrSelections) if (values.isNotEmpty()) params["attr_$code"] = values.joinToString(",")
        for ((code, range) in attrRanges) {
            if (range.first.isNotBlank()) params["attr_${code}_min"] = range.first
            if (range.second.isNotBlank()) params["attr_${code}_max"] = range.second
        }
        return params
    }

    fun search() {
        page = 1
        loading = true
        error = null
        viewModelScope.launch {
            when (val result = listingRepository.getListings(buildParams(1))) {
                is ApiResult.Success -> {
                    listings = result.data.listings
                    hasMore = result.data.page < result.data.pages
                }
                is ApiResult.Error -> error = result.message
            }
            loading = false
        }
    }

    fun loadMore() {
        if (loadingMore || !hasMore) return
        loadingMore = true
        val nextPage = page + 1
        viewModelScope.launch {
            when (val result = listingRepository.getListings(buildParams(nextPage))) {
                is ApiResult.Success -> {
                    listings = listings + result.data.listings
                    page = nextPage
                    hasMore = result.data.page < result.data.pages
                }
                is ApiResult.Error -> { /* keep current page on a load-more failure */ }
            }
            loadingMore = false
        }
    }
}
