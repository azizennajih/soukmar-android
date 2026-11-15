package com.soukmar.app.data.remote.dto

import kotlinx.serialization.Serializable

/** Advertising/affiliate banner managed in the web admin area (GET banners). */
@Serializable
data class SponsorBannerDto(
    val id: String,
    val imageUrl: String,
    val linkUrl: String,
    val altText: String = "",
    val placement: String = "INLINE"
)
