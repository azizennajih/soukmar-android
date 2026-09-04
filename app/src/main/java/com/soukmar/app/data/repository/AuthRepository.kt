package com.soukmar.app.data.repository

import com.soukmar.app.data.local.TokenManager
import com.soukmar.app.data.remote.ApiService
import com.soukmar.app.data.remote.dto.*
import kotlinx.serialization.json.Json
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val unverified: Boolean = false) : ApiResult<Nothing>()
}

@Singleton
class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val tokenManager: TokenManager,
    private val json: Json
) {
    private fun <T> parseError(response: Response<T>): ApiResult.Error {
        val body = response.errorBody()?.string()
        val parsed = try {
            body?.let { json.decodeFromString(ApiErrorDto.serializer(), it) }
        } catch (e: Exception) { null }
        return ApiResult.Error(parsed?.error ?: "Une erreur est survenue.", parsed?.unverified ?: false)
    }

    suspend fun login(email: String, password: String): ApiResult<UserDto> {
        return try {
            val res = api.login(LoginRequest(email, password))
            if (res.isSuccessful && res.body() != null) {
                val body = res.body()!!
                tokenManager.saveSession(body.token, body.user.id, body.user.name, body.user.email, body.user.role)
                ApiResult.Success(body.user)
            } else parseError(res)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Erreur réseau.")
        }
    }

    suspend fun register(name: String, email: String, password: String, phone: String?, city: String?): ApiResult<Boolean> {
        return try {
            val res = api.register(RegisterRequest(name, email, password, phone, city))
            if (res.isSuccessful) ApiResult.Success(true) else parseError(res)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Erreur réseau.")
        }
    }

    suspend fun resendVerification(email: String): ApiResult<Boolean> {
        return try {
            val res = api.resendVerification(ForgotPasswordRequest(email))
            if (res.isSuccessful) ApiResult.Success(true) else parseError(res)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Erreur réseau.")
        }
    }

    suspend fun forgotPassword(email: String): ApiResult<Boolean> {
        return try {
            val res = api.forgotPassword(ForgotPasswordRequest(email))
            if (res.isSuccessful) ApiResult.Success(true) else parseError(res)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Erreur réseau.")
        }
    }

    suspend fun resetPassword(token: String, password: String): ApiResult<Boolean> {
        return try {
            val res = api.resetPassword(ResetPasswordRequest(token, password))
            if (res.isSuccessful) ApiResult.Success(true) else parseError(res)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Erreur réseau.")
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): ApiResult<Boolean> {
        return try {
            val res = api.changePassword(ChangePasswordRequest(currentPassword, newPassword))
            if (res.isSuccessful) ApiResult.Success(true) else parseError(res)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Erreur réseau.")
        }
    }

    suspend fun getMe(): ApiResult<UserDto> {
        return try {
            val res = api.me()
            if (res.isSuccessful && res.body() != null) ApiResult.Success(res.body()!!) else parseError(res)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Erreur réseau.")
        }
    }

    suspend fun updateProfile(name: String, phone: String?, city: String?): ApiResult<UserDto> {
        return try {
            val res = api.updateProfile(ProfileUpdateRequest(name, phone, city))
            if (res.isSuccessful && res.body() != null) ApiResult.Success(res.body()!!) else parseError(res)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Erreur réseau.")
        }
    }

    suspend fun updateProfileImage(imageUrl: String): ApiResult<UserDto> {
        return try {
            val res = api.updateProfileImage(ProfileImageUpdateRequest(imageUrl))
            if (res.isSuccessful && res.body() != null) ApiResult.Success(res.body()!!) else parseError(res)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Erreur réseau.")
        }
    }

    suspend fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()

    suspend fun logout() = tokenManager.clear()
}
