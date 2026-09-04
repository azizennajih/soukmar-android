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

@Serializable
data class ReviewAuthorDto(
    val id: String,
    val name: String,
    val image: String? = null
)

@Serializable
data class ReviewListingRefDto(
    val id: String,
    val title: String
)

/** Mirrors soukmar-backend's GET /api/reviews/user/:userId — a review
 * received by that user, with the reviewer and listing it was left on. */
@Serializable
data class ReviewWithDetailsDto(
    val id: String,
    val listingId: String,
    val reviewerId: String,
    val revieweeId: String,
    val rating: Int,
    val comment: String? = null,
    val createdAt: String,
    val reviewer: ReviewAuthorDto? = null,
    val listing: ReviewListingRefDto? = null
)

@Serializable
data class ReviewsForUserResponse(
    val reviews: List<ReviewWithDetailsDto> = emptyList(),
    val avgRating: Double? = null,
    val count: Int = 0
)

/** Mirrors soukmar-backend's GET /api/users/:id/profile. */
@Serializable
data class SellerProfileDto(
    val id: String,
    val name: String,
    val city: String? = null,
    val image: String? = null,
    val createdAt: String,
    val avgRating: Double? = null,
    val reviewCount: Int = 0,
    val activeListingsCount: Int = 0,
    val avgResponseHours: Double? = null
)
