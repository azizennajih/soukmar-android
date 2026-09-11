package com.soukmar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.soukmar.app.ui.i18n.t
import com.soukmar.app.ui.model.DIAL_CODES
import com.soukmar.app.ui.model.composePhone
import com.soukmar.app.ui.model.dialCodeByIso
import com.soukmar.app.ui.model.flagEmoji
import com.soukmar.app.ui.model.parsePhone
import com.soukmar.app.ui.theme.BorderColor
import com.soukmar.app.ui.theme.Primary
import com.soukmar.app.ui.theme.TextMuted

/** Drop-in replacement for a plain phone `AppTextField`: owns its own
 * iso/localNumber state, parsed from [value] and composed back into it on
 * change — mirrors the web app's `PhoneInputComponent` (`ngOnChanges` +
 * echo-skip so typing doesn't get fought by re-parsing our own emission).
 * [value] is the full stored string, e.g. "+212612345678". */
@Composable
fun PhoneInputField(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    var iso by remember { mutableStateOf("MA") }
    var localNumber by remember { mutableStateOf("") }

    LaunchedEffect(value) {
        if (composePhone(iso, localNumber) == value) return@LaunchedEffect
        val (parsedIso, parsedLocal) = parsePhone(value)
        iso = parsedIso
        localNumber = parsedLocal
    }

    PhoneInput(
        iso = iso,
        localNumber = localNumber,
        onIsoChange = { iso = it; onValueChange(composePhone(it, localNumber)) },
        onLocalNumberChange = { localNumber = it; onValueChange(composePhone(iso, it)) },
        modifier = modifier,
    )
}

/** Country-code (flag + dial code) picker + local-number field that together
 * compose one dial-code-prefixed string (e.g. "+212612345678") — mirrors the
 * web app's `app-phone-input` ([value]/[onValueChange], parses/composes via
 * the same rules as `dial-codes.ts`, see `ui/model/DialCodes.kt`). */
@Composable
fun PhoneInput(
    iso: String,
    localNumber: String,
    onIsoChange: (String) -> Unit,
    onLocalNumberChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var pickerOpen by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(1.5.dp, BorderColor, RoundedCornerShape(12.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .clickable { pickerOpen = true }
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(flagEmoji(iso))
            Spacer(Modifier.width(6.dp))
            Text(dialCodeByIso(iso).dialCode, color = TextMuted)
        }
        Box(Modifier.width(1.dp).height(28.dp).background(BorderColor))
        OutlinedTextField(
            value = localNumber,
            onValueChange = onLocalNumberChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("6 00 00 00 00") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                cursorColor = Primary,
            ),
        )
    }

    if (pickerOpen) {
        CountryPickerDialog(
            selectedIso = iso,
            onSelect = { onIsoChange(it); pickerOpen = false },
            onDismiss = { pickerOpen = false },
        )
    }
}

@Composable
private fun CountryPickerDialog(selectedIso: String, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) DIAL_CODES
        else DIAL_CODES.filter { it.name.lowercase().contains(q) || it.dialCode.contains(q) || it.iso.lowercase() == q }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxHeight(0.75f)) {
            Column(Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(t("common.search")) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, cursorColor = Primary),
                )
                Spacer(Modifier.height(8.dp))
                if (filtered.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(t("common.no_results"), color = TextMuted)
                    }
                } else {
                    LazyColumn {
                        items(filtered, key = { it.iso }) { entry ->
                            val isSelected = entry.iso == selectedIso
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(entry.iso) }
                                    .background(if (isSelected) Primary.copy(alpha = 0.08f) else androidx.compose.ui.graphics.Color.Transparent)
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(flagEmoji(entry.iso))
                                Spacer(Modifier.width(10.dp))
                                Text(entry.name, modifier = Modifier.weight(1f))
                                Text(entry.dialCode, color = TextMuted)
                            }
                        }
                    }
                }
            }
        }
    }
}
