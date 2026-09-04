package com.soukmar.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SuccessDto(val success: Boolean = true)

@Serializable
data class OkDto(val ok: Boolean = true)
