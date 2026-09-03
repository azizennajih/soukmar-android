package com.soukmar.app.data.repository

import com.soukmar.app.data.remote.ApiService
import com.soukmar.app.data.remote.dto.ApiErrorDto
import com.soukmar.app.data.remote.dto.ListingsResponseDto
import kotlinx.serialization.json.Json
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ListingRepository @Inject constructor(
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

    /** [filters] mirrors the web ListingFilters shape flattened into query
     * params, including dynamic `attr_<CODE>` / `attr_<CODE>_min` / `_max`
     * entries for EAV attribute filtering. */
    suspend fun getListings(filters: Map<String, String>): ApiResult<ListingsResponseDto> {
        return try {
            val res = api.getListings(filters)
            if (res.isSuccessful && res.body() != null) ApiResult.Success(res.body()!!) else parseError(res)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Erreur réseau.")
        }
    }
}
