package com.soukmar.app.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ListingUserDto(
    val id: String,
    val name: String,
    val city: String? = null
)

@Serializable
data class ListingAttributeValueDto(
    val id: String,
    val attributeDefinitionId: String,
    val attributeDefinition: AttributeDefinitionDto? = null,
    val valueText: String? = null,
    val valueNumber: Double? = null,
    val valueBoolean: Boolean? = null
)

@Serializable
data class ListingDto(
    val id: String,
    val title: String,
    val description: String = "",
    val price: Double? = null,
    val currency: String = "MAD",
    val category: String,
    val subcategoryId: String? = null,
    val condition: String? = null,
    val city: String,
    val region: String? = null,
    val images: List<String> = emptyList(),
    val status: String = "ACTIVE",
    val isPremium: Boolean = false,
    val isFeatured: Boolean = false,
    val views: Int = 0,
    val phone: String? = null,
    val whatsapp: String? = null,
    val showPhone: Boolean? = null,
    val userId: String,
    val user: ListingUserDto? = null,
    val attributeValues: List<ListingAttributeValueDto> = emptyList(),
    val avgPrice: Double? = null,
    val bumpedAt: String? = null,
    val createdAt: String
)

@Serializable
data class ListingsResponseDto(
    val listings: List<ListingDto> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val pages: Int = 1
)

@Serializable
data class FavoriteRecordDto(
    val id: String? = null,
    val userId: String? = null,
    val listingId: String? = null
)

/** Body for both POST /listings and PUT /listings/:id — mirrors the web
 * deposer-annonce component, which reuses the same payload object for
 * create and update rather than sending a partial diff. */
@Serializable
data class ListingUpsertRequest(
    val title: String,
    val description: String,
    val price: Double? = null,
    val currency: String = "MAD",
    val category: String,
    val subcategoryId: String? = null,
    val condition: String? = null,
    val city: String,
    val images: List<String> = emptyList(),
    val phone: String? = null,
    val whatsapp: String? = null,
    val showPhone: Boolean = true,
    val attributes: Map<String, JsonElement> = emptyMap()
)

@Serializable
data class UploadResponseDto(val urls: List<String> = emptyList())
