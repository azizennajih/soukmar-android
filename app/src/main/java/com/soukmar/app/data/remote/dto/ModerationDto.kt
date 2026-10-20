package com.soukmar.app.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

/** Mirrors the staff API of the listing review workflow (`/api/admin/moderation`, backend routes/moderation.ts).
 * Only ADMIN and MODERATOR accounts get answers; everything is checked again on the server. */

@Serializable
data class ModerationSummaryDto(
    val pending: Int = 0,
    val changesRequested: Int = 0,
    val suspended: Int = 0,
    val decidedToday: Int = 0,
    val highRiskPending: Int = 0,
    val oldestPendingSince: String? = null
)

@Serializable
data class ModerationSignalDto(
    val code: String,
    val severity: String = "low",
    val message: String = "",
    val params: Map<String, JsonElement>? = null
) {
    /** `{name}` values for the translated text of the code; numbers and strings alike. */
    fun paramStrings(): Array<Pair<String, String>> =
        params.orEmpty().map { (k, v) -> k to ((v as? JsonPrimitive)?.content ?: v.toString()) }.toTypedArray()
}

@Serializable
data class ModerationSellerShortDto(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val emailVerified: Boolean = false
)

@Serializable
data class ModerationQueueItemDto(
    val id: String,
    val title: String,
    val category: String,
    val country: String = "MA",
    val city: String = "",
    val price: Double? = null,
    val priceType: String = "FIXED",
    val currency: String = "MAD",
    val status: String,
    val images: List<String> = emptyList(),
    val imageCount: Int = 0,
    val createdAt: String,
    val submittedAt: String? = null,
    val moderationReason: String? = null,
    val moderationSignals: List<ModerationSignalDto> = emptyList(),
    val riskScore: Int = 0,
    val user: ModerationSellerShortDto? = null
)

@Serializable
data class ModerationQueueDto(
    val total: Int = 0,
    val page: Int = 1,
    val pages: Int = 1,
    val listings: List<ModerationQueueItemDto> = emptyList()
)

@Serializable
data class ModerationListingDto(
    val id: String,
    val title: String,
    val description: String = "",
    val price: Double? = null,
    val priceType: String = "FIXED",
    val currency: String = "MAD",
    val category: String,
    val city: String = "",
    val country: String = "MA",
    val images: List<String> = emptyList(),
    val status: String,
    val phone: String? = null,
    val whatsapp: String? = null,
    val moderationReason: String? = null,
    val moderationSignals: List<ModerationSignalDto> = emptyList()
)

@Serializable
data class ModerationSellerDto(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String? = null,
    val emailVerified: Boolean = false,
    val phoneVerified: Boolean = false,
    val idVerified: Boolean = false,
    val createdAt: String? = null,
    val listingsTotal: Int = 0,
    val listingsPublic: Int = 0,
    val listingsRejected: Int = 0,
    val reportsAgainst: Int = 0
)

@Serializable
data class ModerationDuplicateListingDto(
    val id: String,
    val title: String = "",
    val status: String = ""
)

@Serializable
data class ModerationDuplicateDto(
    val code: String,
    val severity: String = "low",
    val message: String = "",
    val params: Map<String, JsonElement>? = null,
    val listing: ModerationDuplicateListingDto? = null
) {
    fun paramStrings(): Array<Pair<String, String>> =
        params.orEmpty().map { (k, v) -> k to ((v as? JsonPrimitive)?.content ?: v.toString()) }.toTypedArray()
}

@Serializable
data class ModerationEventDto(
    val id: String,
    val fromStatus: String? = null,
    val toStatus: String,
    val actorName: String? = null,
    val actorRole: String = "SYSTEM",
    val reason: String? = null,
    val emailKind: String? = null,
    val emailStatus: String? = null,
    val emailSentAt: String? = null,
    val emailError: String? = null,
    val createdAt: String
)

@Serializable
data class ModerationDetailDto(
    val listing: ModerationListingDto,
    val riskScore: Int = 0,
    val seller: ModerationSellerDto = ModerationSellerDto(),
    val duplicates: List<ModerationDuplicateDto> = emptyList(),
    val events: List<ModerationEventDto> = emptyList(),
    /** snake_case names: approve, reject, request_changes, suspend, requeue */
    val allowedActions: List<String> = emptyList()
)

@Serializable
data class ModerationDecisionRequest(
    val reason: String? = null,
    val allowResubmit: Boolean? = null,
    /** The status the admin saw; the server refuses the decision (409) if another reviewer was faster. */
    val expectedStatus: String? = null
)

@Serializable
data class ModerationDecisionEventDto(val emailStatus: String? = null)

@Serializable
data class ModerationDecisionResponseDto(
    val ok: Boolean = true,
    val status: String? = null,
    val event: ModerationDecisionEventDto? = null
)

@Serializable
data class ModerationResendResponseDto(val ok: Boolean = false, val emailStatus: String? = null)
