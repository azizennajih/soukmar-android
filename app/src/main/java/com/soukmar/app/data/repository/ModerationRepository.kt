package com.soukmar.app.data.repository

import com.soukmar.app.data.remote.ApiService
import com.soukmar.app.data.remote.dto.ApiErrorDto
import com.soukmar.app.data.remote.dto.ModerationDecisionRequest
import com.soukmar.app.data.remote.dto.ModerationDecisionResponseDto
import com.soukmar.app.data.remote.dto.ModerationDetailDto
import com.soukmar.app.data.remote.dto.ModerationQueueDto
import com.soukmar.app.data.remote.dto.ModerationSummaryDto
import kotlinx.serialization.json.Json
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

/** Outcome of a staff decision: the server answers 409 when another reviewer decided first. */
sealed class DecisionResult {
    data class Done(val emailStatus: String?) : DecisionResult()
    data object Conflict : DecisionResult()
    data object ReasonInvalid : DecisionResult()
    data class Failed(val message: String) : DecisionResult()
}

@Singleton
class ModerationRepository @Inject constructor(
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

    suspend fun summary(): ApiResult<ModerationSummaryDto> = try {
        val res = api.getModerationSummary()
        if (res.isSuccessful && res.body() != null) ApiResult.Success(res.body()!!) else parseError(res)
    } catch (e: Exception) { ApiResult.Error(e.message ?: "Erreur réseau.") }

    suspend fun queue(status: String, country: String, category: String, query: String, sort: String, page: Int): ApiResult<ModerationQueueDto> = try {
        val res = api.getModerationQueue(
            status = status,
            country = country.takeIf { it.isNotBlank() },
            category = category.takeIf { it.isNotBlank() },
            q = query.trim().takeIf { it.isNotBlank() },
            sort = sort,
            page = page
        )
        if (res.isSuccessful && res.body() != null) ApiResult.Success(res.body()!!) else parseError(res)
    } catch (e: Exception) { ApiResult.Error(e.message ?: "Erreur réseau.") }

    suspend fun detail(id: String): ApiResult<ModerationDetailDto> = try {
        val res = api.getModerationDetail(id)
        if (res.isSuccessful && res.body() != null) ApiResult.Success(res.body()!!) else parseError(res)
    } catch (e: Exception) { ApiResult.Error(e.message ?: "Erreur réseau.") }

    /** [action] is the URL form: approve | reject | request-changes | suspend | requeue. */
    suspend fun decide(id: String, action: String, reason: String?, allowResubmit: Boolean?, expectedStatus: String): DecisionResult = try {
        val res: Response<ModerationDecisionResponseDto> = api.decideListing(
            id, action, ModerationDecisionRequest(reason?.takeIf { it.isNotBlank() }, allowResubmit, expectedStatus)
        )
        when {
            res.isSuccessful -> DecisionResult.Done(res.body()?.event?.emailStatus)
            res.code() == 409 -> DecisionResult.Conflict
            res.code() == 400 -> DecisionResult.ReasonInvalid
            else -> DecisionResult.Failed((parseError(res)).message)
        }
    } catch (e: Exception) { DecisionResult.Failed(e.message ?: "Erreur réseau.") }

    /** True when the mail went out this time. */
    suspend fun resendMail(eventId: String): ApiResult<Boolean> = try {
        val res = api.resendModerationMail(eventId)
        if (res.isSuccessful && res.body() != null) ApiResult.Success(res.body()!!.ok) else parseError(res)
    } catch (e: Exception) { ApiResult.Error(e.message ?: "Erreur réseau.") }
}
