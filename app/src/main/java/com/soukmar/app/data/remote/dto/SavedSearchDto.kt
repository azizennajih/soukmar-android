package com.soukmar.app.data.remote.dto

import kotlinx.serialization.Serializable

/** Mirrors soukmar-backend's SavedSearch model / /api/saved-searches routes.
 * Specific typed columns, not a serialized filter blob — except [attrs],
 * which stores the SELECT/BOOLEAN EAV filter selections only (code -> list
 * of selected option values). NUMBER attribute ranges aren't persisted,
 * matching the web app's saveSearch() which skips _min/_max range keys. */
@Serializable
data class SavedSearchDto(
    val id: String,
    val userId: String,
    val name: String,
    val category: String? = null,
    val subcategoryId: String? = null,
    val q: String? = null,
    val city: String? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val condition: String? = null,
    val attrs: Map<String, List<String>>? = null,
    val createdAt: String
)

@Serializable
data class SavedSearchCreateRequest(
    val name: String,
    val category: String? = null,
    val subcategoryId: String? = null,
    val q: String? = null,
    val city: String? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val condition: String? = null,
    val attrs: Map<String, List<String>>? = null
)
