@file:OptIn(ExperimentalMaterial3Api::class)

package com.soukmar.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.soukmar.app.ui.i18n.tCatalog
import com.soukmar.app.ui.theme.Primary
import java.text.Normalizer

private fun normalizeForSearch(s: String): String =
    Normalizer.normalize(s, Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "").lowercase()

/** Free-text field with catalog-code suggestions, e.g. the Jobs category's
 * "Beruf" field — mirrors the web's TextAutocompleteComponent: the typed
 * value itself is what gets stored (never constrained to one of the
 * suggested codes), suggestions are purely a shortcut. Picking a suggestion
 * writes its *translated label* into the field, matching the web's
 * `pick(opt.label)` (so the stored value is whatever label was current in
 * the active language at pick time, not the raw code). */
@Composable
fun TextAutocompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    options: List<String>,
    labelPrefix: String,
    modifier: Modifier = Modifier,
    placeholder: String = ""
) {
    var expanded by remember { mutableStateOf(false) }
    val labeled = options.map { it to tCatalog("$labelPrefix$it", it) }
    val filtered = if (value.isBlank()) {
        labeled
    } else {
        val q = normalizeForSearch(value)
        labeled.filter { (_, label) -> normalizeForSearch(label).contains(q) }
    }
    val showMenu = expanded && filtered.isNotEmpty()

    ExposedDropdownMenuBox(expanded = showMenu, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { onValueChange(it); expanded = true },
            placeholder = { Text(placeholder) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, cursorColor = Primary)
        )
        ExposedDropdownMenu(expanded = showMenu, onDismissRequest = { expanded = false }, modifier = Modifier.heightIn(max = 320.dp)) {
            filtered.take(60).forEach { (_, label) ->
                DropdownMenuItem(text = { Text(label) }, onClick = { onValueChange(label); expanded = false })
            }
        }
    }
}
