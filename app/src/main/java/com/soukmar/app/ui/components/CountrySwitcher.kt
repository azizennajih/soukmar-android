package com.soukmar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soukmar.app.ui.i18n.LocalI18n
import com.soukmar.app.ui.i18n.t
import com.soukmar.app.ui.model.COUNTRIES
import com.soukmar.app.ui.model.COUNTRY_REGIONS
import com.soukmar.app.ui.model.countryFlag
import com.soukmar.app.ui.model.localeForLang
import com.soukmar.app.ui.model.regionLabelKey
import com.soukmar.app.ui.theme.BorderColor
import com.soukmar.app.ui.theme.Primary
import com.soukmar.app.ui.theme.TextMuted
import com.soukmar.app.ui.theme.TextPrimary
import java.util.Locale

/** Flag+name button opening a searchable, continent-grouped country picker —
 * mirrors the web navbar's country switcher (same ~195 countries, same
 * Morocco-first/continent grouping, same search-to-filter behavior). Placed
 * next to LanguageSwitcher wherever the web navbar would show both. Flag is
 * a plain computed emoji: Android has no equivalent of web's Windows-Chrome
 * flag-rendering bug that forced bundled SVG icons there. */
@Composable
fun CountrySwitcher(country: String, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    val i18n = LocalI18n.current
    val uiLocale = localeForLang(i18n.currentLang)
    fun displayName(code: String) = Locale("", code).getDisplayCountry(uiLocale).ifEmpty { code }

    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    Box(modifier = modifier) {
        TextButton(
            onClick = { expanded = true },
            modifier = Modifier.background(BorderColor.copy(alpha = 0.4f), RoundedCornerShape(999.dp))
        ) {
            // Flag + raw ISO code (not the full display name) to stay as
            // compact as LanguageSwitcher's "🇫🇷 FR" — the top app bar has no
            // room for e.g. "🇵🇬 Papouasie-Nouvelle-Guinée"; the full name only
            // appears once the dropdown itself is open.
            Text("${countryFlag(country)} $country", color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false; query = "" },
            modifier = Modifier.heightIn(max = 420.dp).width(260.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(t("common.search")) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, cursorColor = Primary)
            )
            val q = query.trim()
            fun pick(code: String) { onSelect(code); expanded = false; query = "" }
            if (q.isEmpty()) {
                COUNTRY_REGIONS.forEach { (region, codes) ->
                    if (codes.isEmpty()) return@forEach
                    Text(
                        t(regionLabelKey(region)),
                        fontSize = 11.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                    codes.forEach { code ->
                        DropdownMenuItem(text = { Text("${countryFlag(code)} ${displayName(code)}") }, onClick = { pick(code) })
                    }
                }
            } else {
                COUNTRIES.filter { displayName(it.code).contains(q, ignoreCase = true) }.forEach { c ->
                    DropdownMenuItem(text = { Text("${countryFlag(c.code)} ${displayName(c.code)}") }, onClick = { pick(c.code) })
                }
            }
        }
    }
}
