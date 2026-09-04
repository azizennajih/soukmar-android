package com.soukmar.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ReportRequest(
    val reportedId: String,
    val listingId: String? = null,
    val reason: String
)

@Serializable
data class ReportRecordDto(val id: String? = null)
