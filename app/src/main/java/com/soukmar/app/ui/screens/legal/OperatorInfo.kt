package com.soukmar.app.ui.screens.legal

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.soukmar.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request

/** Provider details for the legal pages. They live in the server's environment (not in
 * the app or the repositories) and come from GET /api/legal/operator; the {op_*}
 * tokens in the legal texts are replaced with them. */
object OperatorInfo {
    private val tokens = mapOf(
        "{op_name}" to "name", "{op_street}" to "street", "{op_zipcity}" to "zipCity", "{op_country}" to "country",
        "{op_phone}" to "phone", "{op_email}" to "email", "{op_vat}" to "vatId"
    )
    private var data by mutableStateOf<JsonObject?>(null)
    private val client = OkHttpClient()

    /** Loads the details once; silently keeps what it has on any failure. */
    suspend fun load() {
        if (data != null) return
        try {
            withContext(Dispatchers.IO) {
                val request = Request.Builder().url(BuildConfig.API_BASE_URL + "legal/operator").build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@use
                    val body = response.body?.string() ?: return@use
                    data = Json.parseToJsonElement(body).jsonObject
                }
            }
        } catch (_: Exception) {
            // offline — the tokens stay empty until the next visit
        }
    }

    /** Replaces the {op_*} tokens of a legal text. */
    fun fill(text: String): String {
        val info = data
        return tokens.entries.fold(text) { out, (token, field) -> out.replace(token, info?.get(field)?.jsonPrimitive?.content ?: "") }
    }
}
