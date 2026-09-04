package com.soukmar.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateConversationRequest(val listingId: String)

@Serializable
data class ChatUserDto(val id: String, val name: String)

@Serializable
data class ChatListingDto(
    val id: String,
    val title: String,
    val price: Double? = null,
    val currency: String = "MAD",
    val images: List<String> = emptyList(),
    val userId: String,
    val status: String = "ACTIVE",
    val user: ChatUserDto
)

/** Mirrors soukmar-backend's Message model. `type` is TEXT | OFFER | SYSTEM,
 * `offerStatus` (OFFER only) is PENDING | ACCEPTED | REJECTED | COUNTERED. */
@Serializable
data class MessageDto(
    val id: String,
    val content: String,
    val type: String,
    val offerAmount: Double? = null,
    val offerStatus: String? = null,
    val isRead: Boolean = false,
    val senderId: String,
    val receiverId: String,
    val listingId: String? = null,
    val conversationId: String? = null,
    val createdAt: String,
    val sender: ChatUserDto? = null
)

/** `messages` is populated (last message only) by GET /chat/conversations
 * for the list preview; empty when fetched some other way. */
@Serializable
data class ConversationDto(
    val id: String,
    val listingId: String,
    val buyerId: String,
    val updatedAt: String = "",
    val listing: ChatListingDto,
    val buyer: ChatUserDto,
    val messages: List<MessageDto> = emptyList()
)

/** True when the signed-in user (their id passed as [myId]) is the listing's
 * seller — the conversation's "other side" is then the buyer, and vice versa.
 * Mirrors ChatComponent.getPartnerId()/getPartnerName() in the web app. */
fun ConversationDto.partnerId(myId: String?): String =
    if (listing.userId == myId) buyerId else listing.userId

fun ConversationDto.partnerName(myId: String?): String =
    if (listing.userId == myId) buyer.name else listing.user.name
