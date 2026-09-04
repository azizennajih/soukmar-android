package com.soukmar.app.data.repository

import com.soukmar.app.data.remote.ApiService
import com.soukmar.app.data.remote.dto.ApiErrorDto
import com.soukmar.app.data.remote.dto.ConversationDto
import com.soukmar.app.data.remote.dto.CreateConversationRequest
import com.soukmar.app.data.remote.dto.MessageDto
import kotlinx.serialization.json.Json
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val api: ApiService,
    private val json: Json
) {
    private fun <T> parseError(response: Response<T>): ApiResult.Error {
        val body = response.errorBody()?.string()
        val parsed = try {
            body?.let { json.decodeFromString(ApiErrorDto.serializer(), it) }
        } catch (e: Exception) { null }
        return ApiResult.Error(parsed?.error ?: "Une erreur est survenue.")
    }

    /** Get-or-create the buyer<->listing conversation. */
    suspend fun startConversation(listingId: String): ApiResult<ConversationDto> {
        return try {
            val res = api.createConversation(CreateConversationRequest(listingId))
            if (res.isSuccessful && res.body() != null) ApiResult.Success(res.body()!!) else parseError(res)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Erreur réseau.")
        }
    }

    suspend fun getConversations(): ApiResult<List<ConversationDto>> {
        return try {
            val res = api.getConversations()
            if (res.isSuccessful && res.body() != null) ApiResult.Success(res.body()!!) else parseError(res)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Erreur réseau.")
        }
    }

    suspend fun getMessages(conversationId: String): ApiResult<List<MessageDto>> {
        return try {
            val res = api.getMessages(conversationId)
            if (res.isSuccessful && res.body() != null) ApiResult.Success(res.body()!!) else parseError(res)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Erreur réseau.")
        }
    }
}
