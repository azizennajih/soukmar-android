package com.soukmar.app.ui.model

import androidx.compose.ui.graphics.Color
import java.text.NumberFormat
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.Locale

/** Mirrors soukmar/src/app/models/listing.model.ts's CATEGORIES/CONDITION_CATEGORIES/
 * HIGHLIGHT_ATTR_CODES — keep in sync when the catalog changes. Labels are
 * French only for now (the app has no i18n layer yet, unlike the web app). */
data class CategoryConfig(
    val value: String,
    val label: String,
    val emoji: String,
    val bg: Color,
    val fg: Color
)

val CATEGORIES: List<CategoryConfig> = listOf(
    CategoryConfig("VEHICLES", "Véhicules", "🚗", Color(0xFFDBEAFE), Color(0xFF1D4ED8)),
    CategoryConfig("REAL_ESTATE", "Immobilier", "🏠", Color(0xFFDCFCE7), Color(0xFF15803D)),
    CategoryConfig("JOBS", "Emploi", "💼", Color(0xFFF3E8FF), Color(0xFF7E22CE)),
    CategoryConfig("ELECTRONICS", "Électronique", "📱", Color(0xFFFEF9C3), Color(0xFFA16207)),
    CategoryConfig("HOME_GARDEN", "Maison & Jardin", "🌿", Color(0xFFD1FAE5), Color(0xFF065F46)),
    CategoryConfig("FASHION", "Mode", "👗", Color(0xFFFCE7F3), Color(0xFFBE185D)),
    CategoryConfig("SERVICES", "Services", "🔧", Color(0xFFFFEDD5), Color(0xFFC2410C)),
    CategoryConfig("OTHER", "Autres", "📦", Color(0xFFF1F5F9), Color(0xFF475569)),
    CategoryConfig("BABY_KIDS", "Bébé & Enfants", "🧸", Color(0xFFCCFBF1), Color(0xFF0F766E)),
    CategoryConfig("PETS", "Animaux", "🐾", Color(0xFFF5E9D9), Color(0xFF92603A)),
    CategoryConfig("SPORTS_LEISURE", "Sport & Loisirs", "⚽", Color(0xFFE0E7FF), Color(0xFF4338CA)),
)

fun categoryConfig(value: String): CategoryConfig? = CATEGORIES.find { it.value == value }

val CONDITION_CATEGORIES: Set<String> = setOf(
    "VEHICLES", "ELECTRONICS", "HOME_GARDEN", "FASHION", "BABY_KIDS", "SPORTS_LEISURE"
)

val HIGHLIGHT_ATTR_CODES: Map<String, List<String>> = mapOf(
    "VEHICLES" to listOf("MILEAGE", "FUEL_TYPE"),
    "ELECTRONICS" to listOf("STORAGE_CAPACITY", "RAM"),
    "REAL_ESTATE" to listOf("LIVING_AREA_SQM", "ROOMS"),
    "FASHION" to listOf("SIZE", "SIZE_EU"),
    "HOME_GARDEN" to listOf("FURNITURE_TYPE"),
)

/** Splits a formatted price into amount/currency so the currency can be
 * rendered smaller — mirrors formatPriceParts() in listing.model.ts. */
fun formatPriceParts(price: Double, currency: String = "MAD"): Pair<String, String> {
    val nf = NumberFormat.getIntegerInstance(Locale.FRANCE)
    return nf.format(price) to currency
}

/** French relative-time label, e.g. "il y a 5 min" — mirrors timeAgo() in
 * listing.model.ts (which uses Intl.RelativeTimeFormat, unavailable here). */
fun timeAgo(isoDate: String): String {
    val instant = try { Instant.parse(isoDate) } catch (e: DateTimeParseException) { return "" }
    val seconds = Duration.between(instant, Instant.now()).seconds.coerceAtLeast(0)
    return when {
        seconds < 60 -> "à l'instant"
        seconds < 3600 -> "il y a ${seconds / 60} min"
        seconds < 86400 -> "il y a ${seconds / 3600} h"
        seconds < 2_592_000 -> "il y a ${seconds / 86400} j"
        else -> "il y a ${seconds / 2_592_000} mois"
    }
}
