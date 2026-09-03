package com.soukmar.app.data.repository

import com.soukmar.app.data.remote.ApiService
import com.soukmar.app.data.remote.dto.ApiErrorDto
import com.soukmar.app.data.remote.dto.CategoryFullResponse
import kotlinx.serialization.json.Json
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatalogRepository @Inject constructor(
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

    /** All subcategories of a category, each with its attribute definitions —
     * one round trip for the search-filter sidebar. */
    suspend fun getCategoryFull(category: String): ApiResult<CategoryFullResponse> {
        return try {
            val res = api.getCategoryFull(category)
            if (res.isSuccessful && res.body() != null) ApiResult.Success(res.body()!!) else parseError(res)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Erreur réseau.")
        }
    }
}
