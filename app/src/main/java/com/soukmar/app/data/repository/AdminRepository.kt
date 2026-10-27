package com.soukmar.app.data.repository

import com.soukmar.app.data.i18n.genericErrorText
import com.soukmar.app.data.remote.ApiService
import com.soukmar.app.data.remote.dto.AdminActivityDto
import com.soukmar.app.data.remote.dto.AdminAnalyticsDto
import com.soukmar.app.data.remote.dto.AdminUserDto
import com.soukmar.app.data.remote.dto.AdminUserRoleRequest
import com.soukmar.app.data.remote.dto.ListingDto
import com.soukmar.app.data.remote.dto.ListingPremiumUpdateRequest
import com.soukmar.app.data.remote.dto.ListingStatusUpdateRequest
import com.soukmar.app.data.remote.dto.SecurityEventDto
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
        return ApiResult.Error(parsed?.error ?: genericErrorText())
    }

    suspend fun getReports(): ApiResult<List<AdminReportDto>> {
        return try {
            val res = api.getAdminReports()
            if (res.isSuccessful && res.body() != null) ApiResult.Success(res.body()!!) else parseError(res)
        } catch (e: Exception) {
            ApiResult.Error(genericErrorText())
        }
    }

    suspend fun updateReport(id: String, status: String, adminNote: String?): ApiResult<AdminReportDto> {
        return try {
            val res = api.updateAdminReport(id, AdminReportUpdateRequest(status, adminNote?.takeIf { it.isNotBlank() }))
            if (res.isSuccessful && res.body() != null) ApiResult.Success(res.body()!!) else parseError(res)
        } catch (e: Exception) {
            ApiResult.Error(genericErrorText())
        }
    }

    suspend fun getIdVerifications(): ApiResult<List<AdminIdVerificationDto>> {
        return try {
            val res = api.getAdminIdVerifications()
            if (res.isSuccessful && res.body() != null) ApiResult.Success(res.body()!!) else parseError(res)
        } catch (e: Exception) {
            ApiResult.Error(genericErrorText())
        }
    }

    suspend fun reviewIdVerification(id: String, status: String, adminNote: String?): ApiResult<AdminIdVerificationDto> {
        return try {
            val res = api.updateAdminIdVerification(id, AdminIdVerificationUpdateRequest(status, adminNote?.takeIf { it.isNotBlank() }))
            if (res.isSuccessful && res.body() != null) ApiResult.Success(res.body()!!) else parseError(res)
        } catch (e: Exception) {
            ApiResult.Error(genericErrorText())
        }
    }

    suspend fun getBoostRequests(): ApiResult<List<BoostRequestDto>> {
        return try {
            val res = api.getAdminBoostRequests()
            if (res.isSuccessful && res.body() != null) ApiResult.Success(res.body()!!) else parseError(res)
        } catch (e: Exception) {
            ApiResult.Error(genericErrorText())
        }
    }

    suspend fun reviewBoostRequest(id: String, status: String, adminNote: String?): ApiResult<BoostRequestDto> {
        return try {
            val res = api.updateAdminBoostRequest(id, BoostRequestReviewRequest(status, adminNote?.takeIf { it.isNotBlank() }))
            if (res.isSuccessful && res.body() != null) ApiResult.Success(res.body()!!) else parseError(res)
        } catch (e: Exception) {
            ApiResult.Error(genericErrorText())
        }
    }

    // ── Dashboard (overview / listings / users / visitors / revenue / security) ─────────────
    // country = null means every country, like the web's "All countries" scope.

    private fun scoped(country: String?, vararg extra: Pair<String, String>): Map<String, String> =
        buildMap { extra.forEach { put(it.first, it.second) }; if (country != null) put("country", country) }

    suspend fun getAllListings(country: String?): ApiResult<List<ListingDto>> = try {
        val res = api.getListings(scoped(country, "limit" to "500", "status" to "ALL"))
        if (res.isSuccessful && res.body() != null) ApiResult.Success(res.body()!!.listings) else parseError(res)
    } catch (e: Exception) { ApiResult.Error(genericErrorText()) }

    suspend fun getUsers(country: String?): ApiResult<List<AdminUserDto>> = try {
        val res = api.getAdminUsers(scoped(country))
        if (res.isSuccessful && res.body() != null) ApiResult.Success(res.body()!!) else parseError(res)
    } catch (e: Exception) { ApiResult.Error(genericErrorText()) }

    suspend fun getActivity(country: String?): ApiResult<List<AdminActivityDto>> = try {
        val res = api.getAdminActivity(scoped(country, "limit" to "40"))
        if (res.isSuccessful && res.body() != null) ApiResult.Success(res.body()!!) else parseError(res)
    } catch (e: Exception) { ApiResult.Error(genericErrorText()) }

    suspend fun getAnalytics(country: String?): ApiResult<AdminAnalyticsDto> = try {
        val res = api.getAdminAnalytics(scoped(country, "days" to "30"))
        if (res.isSuccessful && res.body() != null) ApiResult.Success(res.body()!!) else parseError(res)
    } catch (e: Exception) { ApiResult.Error(genericErrorText()) }

    suspend fun getSecurityEvents(): ApiResult<List<SecurityEventDto>> = try {
        val res = api.getAdminSecurityEvents()
        if (res.isSuccessful && res.body() != null) ApiResult.Success(res.body()!!) else parseError(res)
    } catch (e: Exception) { ApiResult.Error(genericErrorText()) }

    suspend fun setUserRole(id: String, role: String): ApiResult<Boolean> = try {
        val res = api.updateAdminUserRole(id, AdminUserRoleRequest(role))
        if (res.isSuccessful) ApiResult.Success(true) else parseError(res)
    } catch (e: Exception) { ApiResult.Error(genericErrorText()) }

    suspend fun setListingStatus(id: String, status: String): ApiResult<Boolean> = try {
        val res = api.updateListingStatus(id, ListingStatusUpdateRequest(status))
        if (res.isSuccessful) ApiResult.Success(true) else parseError(res)
    } catch (e: Exception) { ApiResult.Error(genericErrorText()) }

    suspend fun setListingPremium(id: String, premium: Boolean): ApiResult<Boolean> = try {
        val res = api.updateListingPremium(id, ListingPremiumUpdateRequest(premium))
        if (res.isSuccessful) ApiResult.Success(true) else parseError(res)
    } catch (e: Exception) { ApiResult.Error(genericErrorText()) }

    suspend fun deleteListing(id: String): ApiResult<Boolean> = try {
        val res = api.deleteListing(id)
        if (res.isSuccessful) ApiResult.Success(true) else parseError(res)
    } catch (e: Exception) { ApiResult.Error(genericErrorText()) }
}
