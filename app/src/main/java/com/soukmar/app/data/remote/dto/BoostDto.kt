package com.soukmar.app.data.remote.dto

import kotlinx.serialization.Serializable

/** Mirrors soukmar-backend's POST /listings/:id/boost-request body. */
@Serializable
data class BoostRequestBody(val tiers: List<String>)

/** Mirrors GET /listings/:id/boost-status. */
@Serializable
data class BoostStatusDto(
    val boostSpotlightUntil: String? = null,
    val boostTopUntil: String? = null,
    val boostGlobalUntil: String? = null,
    val pendingRequest: BoostRequestDto? = null
)

/** Mirrors a BoostRequest row (see soukmar-backend's BoostRequest model) —
 * returned by POST /listings/:id/boost-request and as `pendingRequest`
 * above, and (with listing/user refs attached) by GET /admin/boost-requests. */
@Serializable
data class BoostRequestDto(
    val id: String,
    val listingId: String,
    val userId: String,
    val tiers: List<String> = emptyList(),
    val totalPrice: Double,
    val currency: String,
    val status: String = "PENDING",
    val adminNote: String? = null,
    val createdAt: String,
    val resolvedAt: String? = null,
    val user: AdminIdVerificationUserRefDto? = null,
    val listing: BoostRequestListingRefDto? = null
)

@Serializable
data class BoostRequestListingRefDto(
    val id: String,
    val title: String,
    val images: List<String> = emptyList(),
    val status: String = "ACTIVE"
)

@Serializable
data class BoostRequestReviewRequest(
    val status: String,
    val adminNote: String? = null
)

/** Four original visibility-boost tiers — deliberately not a 1:1 copy of any
 * competitor's tier list (see soukmar/src/app/models/boost.model.ts, the
 * source of truth this mirrors). Prices/durations are kept in sync by hand;
 * the backend re-validates and re-quotes on submit regardless. */
enum class BoostTierId { bump, spotlight, top, global }

data class BoostTier(val id: BoostTierId, val priceMAD: Int, val durationDays: Int?)

val BOOST_TIERS: List<BoostTier> = listOf(
    BoostTier(BoostTierId.bump, 15, null),
    BoostTier(BoostTierId.spotlight, 39, 7),
    BoostTier(BoostTierId.top, 59, 7),
    BoostTier(BoostTierId.global, 89, 10),
)

data class BoostQuote(val subtotal: Int, val discountPercent: Int, val total: Int)

/** Mirrors quoteBoostPrice() in the web's boost.model.ts — 10% off when
 * combining 2+ tiers. */
fun quoteBoostPrice(tierIds: Set<BoostTierId>): BoostQuote {
    val byId = BOOST_TIERS.associateBy { it.id }
    val subtotal = tierIds.sumOf { byId[it]?.priceMAD ?: 0 }
    val discountPercent = if (tierIds.size >= 2) 10 else 0
    val total = Math.round(subtotal * (1 - discountPercent / 100.0)).toInt()
    return BoostQuote(subtotal, discountPercent, total)
}
