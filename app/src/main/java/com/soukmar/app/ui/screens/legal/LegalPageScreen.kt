@file:OptIn(ExperimentalMaterial3Api::class)

package com.soukmar.app.ui.screens.legal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soukmar.app.ui.i18n.t
import com.soukmar.app.ui.theme.BorderColor
import com.soukmar.app.ui.theme.Primary
import com.soukmar.app.ui.theme.TextMuted
import com.soukmar.app.ui.theme.TextPrimary

/** Generic content screen for all four legal pages (mentions légales,
 * politique de confidentialité, conditions d'utilisation, droit de
 * rétractation) — mirrors the web's shared `legal-page.component` (and
 * `mentions-legales.component`, which uses the same `sN_title`/`sN_body`
 * numbered-section pattern under its own `legal.notice` namespace). Content
 * lives entirely in the shared i18n JSON assets, no backend involved. */
@Composable
fun LegalPageScreen(
    titleKey: String,
    namespace: String,
    sectionCount: Int,
    onBack: () -> Unit,
    extraLinks: (@Composable ColumnScope.() -> Unit)? = null
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(t(titleKey)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = t("chat.back")) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            Text(t("legal.last_updated"), color = TextMuted, fontSize = 12.sp)
            Spacer(Modifier.height(16.dp))
            for (n in 1..sectionCount) {
                Text(t("$namespace.s${n}_title"), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                Spacer(Modifier.height(6.dp))
                Text(t("$namespace.s${n}_body"), fontSize = 14.sp, color = TextPrimary, lineHeight = 20.sp)
                Spacer(Modifier.height(18.dp))
            }
            extraLinks?.invoke(this)
        }
    }
}

@Composable
fun LegalLinkRow(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextMuted)
    }
    HorizontalDivider(color = BorderColor)
}
