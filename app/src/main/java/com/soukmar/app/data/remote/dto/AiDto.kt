package com.soukmar.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/** GET ai/status. `configured = false` (no AI machine set up on the server): the assistant is not shown at all. */
@Serializable
data class AiStatusDto(
    val configured: Boolean = false,
    val available: Boolean = false,
    val maxImages: Int = 6
)

@Serializable
data class AiCategorySuggestionDto(
    val category: String,
    @SerialName("subcategory_code") val subcategoryCode: String? = null,
    @SerialName("subcategory_id") val subcategoryId: String? = null
)

/** Answer of POST ai/listing-draft, already validated against the catalog by the backend. */
@Serializable
data class AiListingDraftDto(
    @SerialName("product_name") val productName: String = "",
    @SerialName("suggested_title") val suggestedTitle: String = "",
    @SerialName("suggested_description") val suggestedDescription: String = "",
    @SerialName("suggested_category") val suggestedCategory: AiCategorySuggestionDto,
    @SerialName("alternative_categories") val alternativeCategories: List<AiCategorySuggestionDto> = emptyList(),
    /** values keyed by attribute code of the suggested subcategory (strings or booleans) */
    val attributes: Map<String, JsonElement> = emptyMap(),
    @SerialName("missing_attributes") val missingAttributes: List<String> = emptyList(),
    val confidence: Double = 0.0,
    @SerialName("missing_information") val missingInformation: List<String> = emptyList(),
    val keywords: List<String> = emptyList(),
    val brand: String? = null,
    val cached: Boolean = false
)

/** Error body of the AI routes: `code` is AI_UNAVAILABLE | AI_BUSY | AI_FAILED. */
@Serializable
data class AiErrorDto(val error: String? = null, val code: String? = null)
