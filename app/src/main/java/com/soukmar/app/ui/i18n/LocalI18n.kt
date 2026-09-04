package com.soukmar.app.ui.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import com.soukmar.app.data.i18n.I18nRepository
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeParseException

val LocalI18n = staticCompositionLocalOf<I18nRepository> {
    error("LocalI18n not provided — wrap the app root in CompositionLocalProvider(LocalI18n provides ...)")
}

/** Shorthand for LocalI18n.current.t(key, params) usable directly inside any
 * @Composable, e.g. Text(t("nav.notifications")) or
 * Text(t("seller.responds_hours", "hours" to "3")). Reads [I18nRepository.lang]
 * so every call site recomposes automatically when the language changes. */
@Composable
fun t(key: String, vararg params: Pair<String, String>): String {
    val i18n = LocalI18n.current
    i18n.currentLang // read for recomposition on language change
    return if (params.isEmpty()) i18n.t(key) else i18n.t(key, params.toMap())
}

@Composable
fun tCatalog(key: String, code: String): String {
    val i18n = LocalI18n.current
    i18n.currentLang // read for recomposition on language change
    return i18n.tCatalog(key, code)
}

/** Localized relative-time label built from common.ago/hours/days/minutes,
 * mirroring the plain French-only timeAgo() in ui/model/CatalogModels.kt.
 * The "just now" / "months" cases have no dedicated key in any of the 6
 * language JSON files (the web app doesn't need one — it delegates those
 * to the browser's Intl.RelativeTimeFormat instead), so those two remain
 * French text in every language — a known, minor, documented gap. */
@Composable
fun timeAgoT(isoDate: String): String {
    val i18n = LocalI18n.current
    i18n.currentLang // read for recomposition on language change
    val instant = try { Instant.parse(isoDate) } catch (e: DateTimeParseException) { return "" }
    val seconds = Duration.between(instant, Instant.now()).seconds.coerceAtLeast(0)
    val ago = i18n.t("common.ago")
    return when {
        seconds < 60 -> "à l'instant"
        seconds < 3600 -> "$ago ${seconds / 60} ${i18n.t("common.minutes")}"
        seconds < 86400 -> "$ago ${seconds / 3600} ${i18n.t("common.hours")}"
        seconds < 2_592_000 -> "$ago ${seconds / 86400} ${i18n.t("common.days")}"
        else -> "$ago ${seconds / 2_592_000} mois"
    }
}
