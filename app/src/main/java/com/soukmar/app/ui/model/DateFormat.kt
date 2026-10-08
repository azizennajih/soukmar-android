package com.soukmar.app.ui.model

import android.icu.util.ULocale
import java.time.LocalDate
import java.time.chrono.IsoChronology
import java.time.format.DateTimeFormatterBuilder
import java.time.format.FormatStyle
import java.util.Locale

/** How a country writes a numeric date: order of day/month/year and the separator
 * (DE 05.12.2026, US 12/05/2026, JP 2026/12/05). Mirrors the web's date-format.ts. */
enum class DatePart { DAY, MONTH, YEAR }

data class CountryDateFormat(val order: List<DatePart>, val separator: String)

private val DEFAULT_DATE_FORMAT = CountryDateFormat(listOf(DatePart.DAY, DatePart.MONTH, DatePart.YEAR), "/")
private val dateFormatCache = HashMap<String, CountryDateFormat>()

/** The country's own date convention, read from the platform's locale data — no table to maintain. */
fun dateFormatForCountry(country: String?): CountryDateFormat {
    val cc = country?.uppercase() ?: return DEFAULT_DATE_FORMAT
    if (cc.length != 2 || !cc.all { it in 'A'..'Z' }) return DEFAULT_DATE_FORMAT
    dateFormatCache[cc]?.let { return it }
    val format = try {
        // "und_DE" -> de_DE, "und_JP" -> ja_JP, "und_MA" -> ar_MA (the region's most likely language)
        val likely = ULocale.addLikelySubtags(ULocale("und_$cc"))
        val locale = Locale.forLanguageTag(likely.toLanguageTag())
        val pattern = DateTimeFormatterBuilder.getLocalizedDateTimePattern(FormatStyle.SHORT, null, IsoChronology.INSTANCE, locale)
        val order = pattern.mapNotNull {
            when (it) {
                'd' -> DatePart.DAY
                'M', 'L' -> DatePart.MONTH
                'y', 'u' -> DatePart.YEAR
                else -> null
            }
        }.distinct()
        val separator = pattern.firstOrNull { it == '.' || it == '/' || it == '-' }?.toString() ?: "/"
        if (order.size == 3) CountryDateFormat(order, separator) else DEFAULT_DATE_FORMAT
    } catch (e: Exception) {
        DEFAULT_DATE_FORMAT
    }
    dateFormatCache[cc] = format
    return format
}

/** Numeric date the way the country writes it. Without a year (chat timestamps) the year part is left out. */
fun formatDateForCountry(date: LocalDate, country: String?, withYear: Boolean = true): String {
    val format = dateFormatForCountry(country)
    val parts = format.order.filter { withYear || it != DatePart.YEAR }.map {
        when (it) {
            DatePart.DAY -> "%02d".format(date.dayOfMonth)
            DatePart.MONTH -> "%02d".format(date.monthValue)
            DatePart.YEAR -> date.year.toString()
        }
    }
    return parts.joinToString(format.separator)
}

/** Empty-field hint in the country's order, with the day/month/year words of the UI language (tt.mm.jjjj, dd/mm/yyyy). */
fun datePlaceholder(country: String?, lang: String): String {
    val words = when (lang) {
        "de" -> Triple("tt", "mm", "jjjj")
        "fr" -> Triple("jj", "mm", "aaaa")
        "es" -> Triple("dd", "mm", "aaaa")
        "it" -> Triple("gg", "mm", "aaaa")
        else -> Triple("dd", "mm", "yyyy")
    }
    val format = dateFormatForCountry(country)
    return format.order.joinToString(format.separator) {
        when (it) {
            DatePart.DAY -> words.first
            DatePart.MONTH -> words.second
            DatePart.YEAR -> words.third
        }
    }
}
