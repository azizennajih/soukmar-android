package com.soukmar.app.data.remote.dto

import kotlinx.serialization.Serializable

/** Mirrors soukmar-backend's /api/reviews/can-review/:listingId response. */
@Serializable
data class CanReviewResponse(
    val canReview: Boolean = false,
    val revieweeId: String? = null,
    val alreadyReviewed: Boolean = false
)

@Serializable
data class ReviewSubmitRequest(
    val listingId: String,
    val revieweeId: String,
    val rating: Int,
    val comment: String? = null
)

@Serializable
data class ReviewDto(
    val id: String,
    val rating: Int,
    val comment: String? = null
)
