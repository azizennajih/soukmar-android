package com.soukmar.app.data.repository

import com.soukmar.app.data.remote.ApiService
import com.soukmar.app.data.remote.dto.FcmTokenRequest
import javax.inject.Inject
import javax.inject.Singleton

/** Registers/unregisters this device's FCM token with the backend, so
 * sendPushToUser can reach it — see soukmar-backend's src/lib/fcm.ts. */
@Singleton
class PushRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun registerToken(token: String): Boolean {
        return try { api.registerFcmToken(FcmTokenRequest(token)).isSuccessful } catch (e: Exception) { false }
    }

    suspend fun unregisterToken(token: String): Boolean {
        return try { api.unregisterFcmToken(FcmTokenRequest(token)).isSuccessful } catch (e: Exception) { false }
    }
}
