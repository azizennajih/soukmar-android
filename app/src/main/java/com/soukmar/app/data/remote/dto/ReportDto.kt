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

@Serializable
data class ReportUserRefDto(
    val id: String,
    val name: String,
    val email: String
)

@Serializable
data class ReportListingRefDto(
    val id: String,
    val title: String
)

/** Mirrors soukmar-backend's GET /api/reports/admin — a moderation-queue
 * row with reporter/reported/listing refs attached. */
@Serializable
data class AdminReportDto(
    val id: String,
    val listingId: String? = null,
    val reporterId: String,
    val reportedId: String,
    val reason: String,
    val status: String,
    val adminNote: String? = null,
    val createdAt: String,
    val resolvedAt: String? = null,
    val reporter: ReportUserRefDto? = null,
    val reported: ReportUserRefDto? = null,
    val listing: ReportListingRefDto? = null
)

@Serializable
data class AdminReportUpdateRequest(
    val status: String,
    val adminNote: String? = null
)
