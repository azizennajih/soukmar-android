package com.soukmar.app.data.remote.dto

import kotlinx.serialization.Serializable

/** Admin dashboard data (web: AdminComponent) — users, activity feed, visitor analytics, security log. */
@Serializable
data class AdminUserCountDto(val listings: Int = 0)

@Serializable
data class AdminUserDto(
    val id: String,
    val name: String = "",
    val email: String = "",
    val role: String = "USER",
    val city: String? = null,
    val country: String? = null,
    val phone: String? = null,
    val createdAt: String = "",
    val _count: AdminUserCountDto? = null
)

@Serializable
data class AdminUserRoleRequest(val role: String)

@Serializable
data class AdminActivityDto(
    val type: String,
    val at: String = "",
    val country: String? = null,
    val title: String = "",
    val detail: String? = null,
    val amount: Double? = null,
    val currency: String? = null,
    val status: String? = null
)

@Serializable
data class AdminCountryCountDto(val country: String, val count: Int = 0, val visitors: Int = 0)

@Serializable
data class AdminActiveNowDto(val total: Int = 0, val byCountry: List<AdminCountryCountDto> = emptyList())

@Serializable
data class AdminTodayDto(val total: Int = 0, val byCountry: List<AdminCountryCountDto> = emptyList())

@Serializable
data class AdminDailyDto(val date: String, val visitors: Int = 0)

@Serializable
data class AdminAnalyticsDto(
    val days: Int = 30,
    val activeNow: AdminActiveNowDto = AdminActiveNowDto(),
    val today: AdminTodayDto = AdminTodayDto(),
    val daily: List<AdminDailyDto> = emptyList(),
    val byCountry: List<AdminCountryCountDto> = emptyList()
)

@Serializable
data class SecurityEventUserDto(val id: String = "", val name: String = "", val email: String = "")

@Serializable
data class SecurityEventDto(
    val id: String,
    val type: String,
    val severity: String = "LOW",
    val ip: String? = null,
    val userId: String? = null,
    val detail: String? = null,
    val createdAt: String = "",
    val user: SecurityEventUserDto? = null
)

@Serializable
data class ListingPremiumUpdateRequest(val isPremium: Boolean)
