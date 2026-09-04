package com.soukmar.app.data.repository

import com.soukmar.app.data.remote.ApiService
import com.soukmar.app.data.remote.dto.ApiErrorDto
import com.soukmar.app.data.remote.dto.NotificationDto
import kotlinx.serialization.json.Json
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
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

    suspend fun getAll(): ApiResult<List<NotificationDto>> {
        return try {
            val res = api.getNotifications()
            if (res.isSuccessful && res.body() != null) ApiResult.Success(res.body()!!) else parseError(res)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Erreur réseau.")
        }
    }

    suspend fun getUnreadCount(): Int {
        return try {
            val res = api.getUnreadNotificationCount()
            if (res.isSuccessful) res.body()?.count ?: 0 else 0
        } catch (e: Exception) {
            0
        }
    }

    suspend fun markRead(id: String): Boolean {
        return try { api.markNotificationRead(id).isSuccessful } catch (e: Exception) { false }
    }

    suspend fun markAllRead(): Boolean {
        return try { api.markAllNotificationsRead().isSuccessful } catch (e: Exception) { false }
    }
}
