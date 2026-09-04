package com.soukmar.app.data.i18n

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.soukmar.app.data.local.LocalePreferences
import com.soukmar.app.ui.model.humanizeCode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import javax.inject.Inject
import javax.inject.Singleton

data class LanguageOption(val code: String, val flag: String, val label: String)

val SUPPORTED_LANGUAGES = listOf(
    LanguageOption("fr", "🇫🇷", "FR"),
    LanguageOption("en", "🇬🇧", "EN"),
    LanguageOption("ar", "🇲🇦", "عر"),
    LanguageOption("de", "🇩🇪", "DE"),
    LanguageOption("es", "🇪🇸", "ES"),
    LanguageOption("it", "🇮🇹", "IT")
)

/** Ports the web app's I18nService 1:1: same 6 languages, same JSON files
 * (copied verbatim into assets/i18n/), same dotted-key nested-object lookup,
 * same single-brace {param} interpolation, same fr fallback, same "missing
 * key renders as the raw key" behavior (no per-key language fallback). */
@Singleton
class I18nRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val localePreferences: LocalePreferences
) {
    companion object {
        const val DEFAULT_LANGUAGE = "fr"
    }

    var currentLang by mutableStateOf(DEFAULT_LANGUAGE)
        private set

    val isRtl: Boolean get() = currentLang == "ar"

    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true }

    private val dictionaries: Map<String, JsonObject> by lazy {
        SUPPORTED_LANGUAGES.associate { option ->
            val text = context.assets.open("i18n/${option.code}.json").bufferedReader().use { it.readText() }
            option.code to json.parseToJsonElement(text).jsonObject
        }
    }

    init {
        repoScope.launch {
            val saved = localePreferences.getLanguage()
            val resolved = saved?.takeIf { code -> SUPPORTED_LANGUAGES.any { it.code == code } } ?: DEFAULT_LANGUAGE
            withContext(Dispatchers.Main.immediate) { currentLang = resolved }
        }
    }

    fun setLang(code: String) {
        if (code == currentLang || SUPPORTED_LANGUAGES.none { it.code == code }) return
        currentLang = code
        repoScope.launch { localePreferences.setLanguage(code) }
    }

    fun t(key: String, params: Map<String, String> = emptyMap()): String {
        val dict = dictionaries[currentLang] ?: dictionaries.getValue(DEFAULT_LANGUAGE)
        var node: JsonElement? = dict
        for (part in key.split(".")) {
            node = (node as? JsonObject)?.get(part) ?: return key
        }
        val raw = (node as? JsonPrimitive)?.takeIf { it.isString }?.content ?: return key
        if (params.isEmpty()) return raw
        var result = raw
        for ((k, v) in params) result = result.replace("{$k}", v)
        return result
    }

    /** For catalog codes (categories/subcategories/EAV attribute+option
     * codes) that come from the backend dynamically — falls back to the old
     * humanizeCode() cosmetic transform when no curated translation exists,
     * rather than showing the raw dotted key like a missing UI-copy key would. */
    fun tCatalog(key: String, code: String): String {
        val result = t(key)
        return if (result == key) humanizeCode(code) else result
    }
}
