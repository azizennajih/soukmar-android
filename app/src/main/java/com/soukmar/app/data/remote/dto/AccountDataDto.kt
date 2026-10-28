package com.soukmar.app.data.remote.dto

import kotlinx.serialization.Serializable

/** Which optional e-mails the account receives (web: /mes-donnees). Sent whole on every change. */
@Serializable
data class EmailPreferencesDto(
    val emailMessages: Boolean = true,
    val emailSavedSearches: Boolean = true,
    val emailListingReminders: Boolean = true
)

/** An account the user has blocked, shown with the public (shortened) name. */
@Serializable
data class BlockedUserDto(
    val id: String,
    val name: String = "",
    val image: String? = null,
    val blockedAt: String = ""
)
