package com.soukmar.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val role: String = "USER",
    val phone: String? = null,
    val city: String? = null,
    val image: String? = null
)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class LoginResponse(val user: UserDto, val token: String)

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val phone: String? = null,
    val city: String? = null
)

@Serializable
data class MessageResponse(val message: String? = null, @SerialName("emailSent") val emailSent: Boolean = false)

@Serializable
data class ForgotPasswordRequest(val email: String)

@Serializable
data class ResetPasswordRequest(val token: String, val password: String)

@Serializable
data class ChangePasswordRequest(val currentPassword: String, val newPassword: String)

@Serializable
data class ApiErrorDto(val error: String? = null, val unverified: Boolean = false)
