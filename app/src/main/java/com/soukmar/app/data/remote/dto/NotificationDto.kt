package com.soukmar.app.data.remote.dto

import kotlinx.serialization.Serializable

/** Mirrors soukmar-backend's Notification model / /api/notifications routes.
 * No dedicated title/body field — display text is built client-side from
 * [type] + [actorName] + [listingTitle], same as the web app's i18n
 * templates (there's no i18n layer in Android yet, so those are hardcoded
 * French strings — see NotificationsScreen). */
@Serializable
data class NotificationDto(
    val id: String,
    val userId: String,
    val type: String,
    val actorName: String? = null,
    val listingId: String? = null,
    val listingTitle: String? = null,
    val conversationId: String? = null,
    val isRead: Boolean = false,
    val createdAt: String
)

@Serializable
data class UnreadCountResponse(val count: Int = 0)

@Serializable
data class FcmTokenRequest(val token: String)
