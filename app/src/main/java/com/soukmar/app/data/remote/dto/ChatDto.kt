package com.soukmar.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateConversationRequest(val listingId: String)

/** Only the fields this phase needs — the full chat screen (later phase)
 * will read messages/listing/buyer off the same /chat/conversations
 * response and can extend this DTO then. */
@Serializable
data class ConversationDto(
    val id: String,
    val listingId: String,
    val buyerId: String
)
