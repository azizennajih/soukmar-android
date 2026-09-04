package com.soukmar.app.data.repository

import com.soukmar.app.data.remote.ApiService
import com.soukmar.app.data.remote.dto.ApiErrorDto
import com.soukmar.app.data.remote.dto.ListingDto
import com.soukmar.app.data.remote.dto.SellerProfileDto
import kotlinx.serialization.json.Json
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

/** Mirrors soukmar-backend's /api/users routes — public seller-profile data. */
@Singleton
class UserRepository @Inject constructor(
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

    suspend fun getSellerProfile(id: String): ApiResult<SellerProfileDto> {
        return try {
            val res = api.getSellerProfile(id)
            if (res.isSuccessful && res.body() != null) ApiResult.Success(res.body()!!) else parseError(res)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Erreur réseau.")
        }
    }

    suspend fun getSellerListings(id: String): ApiResult<List<ListingDto>> {
        return try {
            val res = api.getSellerListings(id)
            if (res.isSuccessful && res.body() != null) ApiResult.Success(res.body()!!) else parseError(res)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Erreur réseau.")
        }
    }
}
