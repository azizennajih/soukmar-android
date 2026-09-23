package com.soukmar.app.data.repository

import com.soukmar.app.data.remote.ApiService
import com.soukmar.app.data.remote.dto.AdminIdVerificationDto
import com.soukmar.app.data.remote.dto.AdminIdVerificationUpdateRequest
import com.soukmar.app.data.remote.dto.AdminReportDto
import com.soukmar.app.data.remote.dto.AdminReportUpdateRequest
import com.soukmar.app.data.remote.dto.ApiErrorDto
import com.soukmar.app.data.remote.dto.BoostRequestDto
import com.soukmar.app.data.remote.dto.BoostRequestReviewRequest
import kotlinx.serialization.json.Json
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository @Inject constructor(
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

    suspend fun getReports(): ApiResult<List<AdminReportDto>> {
        return try {
            val res = api.getAdminReports()
            if (res.isSuccessful && res.body() != null) ApiResult.Success(res.body()!!) else parseError(res)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Erreur réseau.")
        }
    }

    suspend fun updateReport(id: String, status: String, adminNote: String?): ApiResult<AdminReportDto> {
        return try {
            val res = api.updateAdminReport(id, AdminReportUpdateRequest(status, adminNote?.takeIf { it.isNotBlank() }))
            if (res.isSuccessful && res.body() != null) ApiResult.Success(res.body()!!) else parseError(res)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Erreur réseau.")
        }
    }

    suspend fun getIdVerifications(): ApiResult<List<AdminIdVerificationDto>> {
        return try {
            val res = api.getAdminIdVerifications()
            if (res.isSuccessful && res.body() != null) ApiResult.Success(res.body()!!) else parseError(res)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Erreur réseau.")
        }
    }

    suspend fun reviewIdVerification(id: String, status: String, adminNote: String?): ApiResult<AdminIdVerificationDto> {
        return try {
            val res = api.updateAdminIdVerification(id, AdminIdVerificationUpdateRequest(status, adminNote?.takeIf { it.isNotBlank() }))
            if (res.isSuccessful && res.body() != null) ApiResult.Success(res.body()!!) else parseError(res)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Erreur réseau.")
        }
    }

    suspend fun getBoostRequests(): ApiResult<List<BoostRequestDto>> {
        return try {
            val res = api.getAdminBoostRequests()
            if (res.isSuccessful && res.body() != null) ApiResult.Success(res.body()!!) else parseError(res)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Erreur réseau.")
        }
    }

    suspend fun reviewBoostRequest(id: String, status: String, adminNote: String?): ApiResult<BoostRequestDto> {
        return try {
            val res = api.updateAdminBoostRequest(id, BoostRequestReviewRequest(status, adminNote?.takeIf { it.isNotBlank() }))
            if (res.isSuccessful && res.body() != null) ApiResult.Success(res.body()!!) else parseError(res)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Erreur réseau.")
        }
    }
}
