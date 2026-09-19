package com.soukmar.app.data.country

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.soukmar.app.data.local.CountryPreferences
import com.soukmar.app.ui.model.isKnownCountry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

/** Ports the web app's CountryService 1:1: the country a visitor is
 * browsing/listing in, persisted independently of language and of the auth
 * session (see CountryPreferences), defaulting to Morocco. On a genuine
 * first launch (nothing saved yet) it best-effort auto-detects the visitor's
 * country via a free IP-geolocation lookup, exactly like the web app's
 * CountryService.detectCountryFromIp() — see soukmar/src/app/services/
 * country.service.ts for the source this mirrors. */
@Singleton
class CountryRepository @Inject constructor(
    private val countryPreferences: CountryPreferences
) {
    companion object {
        const val DEFAULT_COUNTRY = "MA"
    }

    var country by mutableStateOf(DEFAULT_COUNTRY)
        private set

    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true }

    // Deliberately a bare OkHttpClient, NOT the app's shared client from
    // NetworkModule — that one auto-attaches our own API Bearer token to
    // every request via an interceptor, which must never be sent to a
    // third-party IP-geolocation service.
    private val plainHttpClient = OkHttpClient()

    init {
        repoScope.launch {
            val saved = countryPreferences.getCountry()
            if (saved != null) {
                val resolved = saved.takeIf { isKnownCountry(it) } ?: DEFAULT_COUNTRY
                withContext(Dispatchers.Main.immediate) { country = resolved }
            } else {
                detectCountryFromIp()
            }
        }
    }

    // Named updateCountry rather than "setCountry" — a plain setCountry
    // clashes at the JVM level with the synthesized property setter Kotlin
    // already generates for `var country` (see ListingsViewModel.selectSort's
    // identical Platform-declaration-clash lesson from an earlier tranche).
    fun updateCountry(code: String) {
        if (code == country) return
        country = code
        repoScope.launch { countryPreferences.setCountry(code) }
    }

    /** Best-effort only: on any failure (no network, unknown country,
     * malformed response) this silently keeps the DEFAULT_COUNTRY — a
     * convenience default is not worth surfacing an error for. */
    private suspend fun detectCountryFromIp() {
        try {
            val request = Request.Builder().url("https://ipapi.co/json/").build()
            val body = plainHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return
                response.body?.string() ?: return
            }
            val code = json.parseToJsonElement(body).jsonObject["country_code"]
                ?.jsonPrimitive?.content?.uppercase() ?: return
            if (isKnownCountry(code)) {
                withContext(Dispatchers.Main.immediate) { country = code }
                countryPreferences.setCountry(code)
            }
        } catch (_: Exception) {
            // Network error, timeout, malformed JSON — keep the default.
        }
    }
}
