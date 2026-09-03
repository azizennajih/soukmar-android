package com.soukmar.app.data.remote

import com.soukmar.app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

/** Mirrors soukmar-backend's /api/auth routes. Grows as more screens land —
 * listings/chat/etc. endpoints will be added alongside their features. */
interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<MessageResponse>

    @POST("auth/resend-verification")
    suspend fun resendVerification(@Body body: ForgotPasswordRequest): Response<MessageResponse>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequest): Response<MessageResponse>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordRequest): Response<MessageResponse>

    @PUT("auth/change-password")
    suspend fun changePassword(@Body body: ChangePasswordRequest): Response<MessageResponse>

    @GET("auth/me")
    suspend fun me(): Response<UserDto>
}
