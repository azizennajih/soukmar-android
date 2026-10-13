package com.soukmar.app.ui.model

import android.content.Context

/** Localised city names by app language ("Munich" -> "München", "Vienna" -> "Wien", ...). The data is
 * the web's `city-names.ts` table, copied verbatim to assets/city-names.txt: one line per city,
 * `Stored name|de|fr|es|it|ar`, an empty slot meaning "same as the stored name". Cities stay stored and
 * filtered under their one canonical spelling — only the *label* changes (see [cityLabel]). */
object CityNames {
    private val COLUMNS = listOf("de", "fr", "es", "it", "ar")

    @Volatile
    private var labels: Map<String, Map<String, String>> = emptyMap()

    /** Reads the asset once at app start; a missing/unreadable file just means "no translations". */
    fun load(context: Context) {
        labels = runCatching {
            val out = COLUMNS.associateWith { mutableMapOf<String, String>() }
            context.assets.open("city-names.txt").bufferedReader().useLines { lines ->
                for (raw in lines) {
                    if (raw.isBlank() || raw.startsWith("#")) continue
                    parseRow(raw, out)
                }
            }
            out
        }.getOrDefault(emptyMap())
    }

    internal fun parseRow(raw: String, out: Map<String, MutableMap<String, String>>) {
        val parts = raw.split("|")
        val city = parts[0].trim()
        COLUMNS.forEachIndexed { i, lang ->
            val label = parts.getOrNull(i + 1)?.trim().orEmpty()
            if (label.isNotEmpty() && label != city) out.getValue(lang)[city] = label
        }
    }

    fun label(lang: String, city: String): String? = labels[lang]?.get(city)
}
