package com.soukmar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.soukmar.app.data.i18n.SUPPORTED_LANGUAGES
import com.soukmar.app.ui.i18n.LocalI18n
import com.soukmar.app.ui.theme.BorderColor
import com.soukmar.app.ui.theme.TextPrimary

/** Flag+code button opening a dropdown of all 6 languages — mirrors the web
 * navbar's language switcher (same flags/labels, same 6 languages). */
@Composable
fun LanguageSwitcher(modifier: Modifier = Modifier) {
    val i18n = LocalI18n.current
    var expanded by remember { mutableStateOf(false) }
    val current = SUPPORTED_LANGUAGES.find { it.code == i18n.currentLang } ?: SUPPORTED_LANGUAGES.first()

    androidx.compose.foundation.layout.Box(modifier = modifier) {
        TextButton(
            onClick = { expanded = true },
            modifier = Modifier.background(BorderColor.copy(alpha = 0.4f), RoundedCornerShape(999.dp))
        ) {
            Text("${current.flag} ${current.label}", color = TextPrimary)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SUPPORTED_LANGUAGES.forEach { option ->
                DropdownMenuItem(
                    text = { Text("${option.flag} ${option.label}") },
                    onClick = { i18n.setLang(option.code); expanded = false },
                    modifier = if (option.code == i18n.currentLang) Modifier.background(BorderColor.copy(alpha = 0.3f)) else Modifier
                )
            }
        }
    }
}
