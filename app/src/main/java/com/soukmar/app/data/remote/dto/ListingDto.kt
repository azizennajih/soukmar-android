package com.soukmar.app.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ListingUserDto(
    val id: String,
    val name: String,
    val city: String? = null,
    val accountType: String? = null,
    val emailVerified: Boolean = false,
    val phoneVerified: Boolean = false,
    val idVerified: Boolean = false
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
    val country: String = "MA",
    val lat: Double? = null,
    val lng: Double? = null,
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
    val createdAt: String,
    val expiresAt: String? = null,
    val expiryExtended: Boolean = false,
    val expiryWarningSent: Boolean = false,
    // Paid visibility boosts (see BoostDto.kt) — null/0 when never bought or expired.
    val boostSpotlightUntil: String? = null,
    val boostTopUntil: String? = null,
    val boostGlobalUntil: String? = null,
    val boostRank: Int = 0
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
 * create and update rather than sending a partial diff. No `currency` field
 * on purpose: the backend derives it server-side from `country`
 * (currencyForCountry) since the international-country tranche — mirrors
 * web's deposer-annonce.component.ts publish() payload, which dropped its
 * own currency field the same way. */
@Serializable
data class ListingUpsertRequest(
    val title: String,
    val description: String,
    val price: Double? = null,
    val category: String,
    val subcategoryId: String? = null,
    val condition: String? = null,
    val city: String,
    val country: String,
    val images: List<String> = emptyList(),
    val phone: String? = null,
    val whatsapp: String? = null,
    val showPhone: Boolean = true,
    val attributes: Map<String, JsonElement> = emptyMap()
)

@Serializable
data class UploadResponseDto(val urls: List<String> = emptyList())

/** Partial PUT body for a status-only change (e.g. mes-annonces' réserver/
 * mettre en vente toggle) — the backend applies whichever fields are
 * present, so this avoids re-sending the full listing form. */
@Serializable
data class ListingStatusUpdateRequest(val status: String)

@Serializable
data class ViewStatDayDto(val date: String, val count: Int)

@Serializable
data class ViewStatsDto(val days: List<ViewStatDayDto> = emptyList(), val total: Int = 0)

/** Mirrors GET /listings/:id/funnel — owner/admin-only conversion funnel
 * (views → favorites → contacts → offers received → offers accepted), each
 * a plain count already tracked elsewhere server-side (Listing.views,
 * Favorite, Conversation, Message), no new analytics infra. */
@Serializable
data class ListingFunnelDto(
    val views: Int = 0,
    val favorites: Int = 0,
    val contacts: Int = 0,
    val offers: Int = 0,
    val offersAccepted: Int = 0
)
