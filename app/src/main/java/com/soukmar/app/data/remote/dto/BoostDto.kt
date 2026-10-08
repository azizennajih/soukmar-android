package com.soukmar.app.data.remote.dto

import kotlinx.serialization.Serializable

/** Mirrors soukmar-backend's POST /listings/:id/boost-request body. */
@Serializable
data class BoostRequestBody(val tiers: List<String>, val withdrawalConsent: Boolean)

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

data class BoostTier(val id: BoostTierId, val durationDays: Int?)

val BOOST_TIERS: List<BoostTier> = listOf(
    BoostTier(BoostTierId.bump, null),
    BoostTier(BoostTierId.spotlight, 7),
    BoostTier(BoostTierId.top, 7),
    BoostTier(BoostTierId.global, 10),
)

/** Boost prices per currency (kept in sync with the web's boost.model.ts and the backend's
 * lib/boosts.ts). A listing in a currency without its own price list is charged in EUR. */
val BOOST_PRICES: Map<String, Map<BoostTierId, Double>> = mapOf(
    "MAD" to mapOf(BoostTierId.bump to 15.0, BoostTierId.spotlight to 39.0, BoostTierId.top to 59.0, BoostTierId.global to 89.0),
    "EUR" to mapOf(BoostTierId.bump to 1.49, BoostTierId.spotlight to 3.99, BoostTierId.top to 5.99, BoostTierId.global to 8.99),
    "USD" to mapOf(BoostTierId.bump to 1.59, BoostTierId.spotlight to 4.29, BoostTierId.top to 6.49, BoostTierId.global to 9.99),
    "GBP" to mapOf(BoostTierId.bump to 1.29, BoostTierId.spotlight to 3.49, BoostTierId.top to 4.99, BoostTierId.global to 7.49),
    "CHF" to mapOf(BoostTierId.bump to 1.49, BoostTierId.spotlight to 3.99, BoostTierId.top to 5.99, BoostTierId.global to 8.99),
)

/** The currency a boost for a listing in [listingCurrency] is charged in. */
fun boostCurrency(listingCurrency: String?): String =
    if (listingCurrency != null && BOOST_PRICES.containsKey(listingCurrency)) listingCurrency else "EUR"

fun tierPrice(id: BoostTierId, currency: String): Double = (BOOST_PRICES[currency] ?: BOOST_PRICES.getValue("EUR"))[id] ?: 0.0

data class BoostQuote(val subtotal: Double, val discountPercent: Int, val total: Double)

private fun cents(n: Double): Double = Math.round(n * 100) / 100.0

/** Mirrors quoteBoostPrice() in the web's boost.model.ts — 10% off when
 * combining 2+ tiers; MAD in whole dirhams, other currencies with cents. */
fun quoteBoostPrice(tierIds: Set<BoostTierId>, currency: String = "MAD"): BoostQuote {
    val subtotal = cents(tierIds.sumOf { tierPrice(it, currency) })
    val discountPercent = if (tierIds.size >= 2) 10 else 0
    val discounted = subtotal * (1 - discountPercent / 100.0)
    val total = if (currency == "MAD") Math.round(discounted).toDouble() else cents(discounted)
    return BoostQuote(subtotal, discountPercent, total)
}
