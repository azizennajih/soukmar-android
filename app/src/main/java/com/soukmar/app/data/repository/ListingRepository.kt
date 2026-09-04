package com.soukmar.app.data.repository

import com.soukmar.app.data.remote.ApiService
import com.soukmar.app.data.remote.dto.ApiErrorDto
import com.soukmar.app.data.remote.dto.ListingDto
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

    suspend fun getListing(id: String): ApiResult<ListingDto> {
        return try {
            val res = api.getListing(id)
            if (res.isSuccessful && res.body() != null) ApiResult.Success(res.body()!!) else parseError(res)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Erreur réseau.")
        }
    }

    /** Mirrors the web app's favorite check: fetch the full list and test
     * membership (there's no dedicated "is this favorited" endpoint). */
    suspend fun getFavoriteIds(): Set<String> {
        return try {
            val res = api.getFavorites()
            if (res.isSuccessful) res.body()?.map { it.id }?.toSet() ?: emptySet() else emptySet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    suspend fun addFavorite(listingId: String): Boolean {
        return try { api.addFavorite(listingId).isSuccessful } catch (e: Exception) { false }
    }

    suspend fun removeFavorite(listingId: String): Boolean {
        return try { api.removeFavorite(listingId).isSuccessful } catch (e: Exception) { false }
    }
}
