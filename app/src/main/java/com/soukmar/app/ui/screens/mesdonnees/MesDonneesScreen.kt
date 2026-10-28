@file:OptIn(ExperimentalMaterial3Api::class)

package com.soukmar.app.ui.screens.mesdonnees

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.soukmar.app.ui.i18n.t
import com.soukmar.app.ui.theme.BorderColor
import com.soukmar.app.ui.theme.Primary
import com.soukmar.app.ui.theme.PrimaryLight
import com.soukmar.app.ui.theme.TextMuted
import com.soukmar.app.ui.theme.TextPrimary
import com.soukmar.app.ui.theme.WhiteColor

@Composable
fun MesDonneesScreen(
    onBack: () -> Unit,
    viewModel: MesDonneesViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) { viewModel.load() }
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(viewModel.message) {
        viewModel.message?.takeIf { it.isNotBlank() }?.let { snackbar.showSnackbar(it) }
        viewModel.clearMessage()
    }

    // The system "save as" dialog lets the user choose where the file goes — no storage permission needed.
    val saveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        val bytes = viewModel.exportBytes
        if (uri == null || bytes == null) return@rememberLauncherForActivityResult
        val written = runCatching { context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) } != null }.getOrDefault(false)
        if (written) viewModel.exportSaved() else viewModel.exportFailed()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(t("mes_donnees.title")) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = t("common.cancel")) } }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionCard {
                Text(t("mes_donnees.email_title"), fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(t("mes_donnees.email_intro"), fontSize = 12.sp, color = TextMuted)
                val prefs = viewModel.prefs
                if (prefs != null) {
                    PrefRow(t("mes_donnees.email_messages"), prefs.emailMessages) { on -> viewModel.setPref { it.copy(emailMessages = on) } }
                    PrefRow(t("mes_donnees.email_saved_searches"), prefs.emailSavedSearches) { on -> viewModel.setPref { it.copy(emailSavedSearches = on) } }
                    PrefRow(t("mes_donnees.email_listing_reminders"), prefs.emailListingReminders) { on -> viewModel.setPref { it.copy(emailListingReminders = on) } }
                    Text(t("mes_donnees.email_always"), fontSize = 11.sp, color = TextMuted)
                }
            }

            SectionCard {
                Text(t("mes_donnees.blocked_title"), fontWeight = FontWeight.Bold, color = TextPrimary)
                val list = viewModel.blocked
                if (list != null && list.isEmpty()) Text(t("mes_donnees.blocked_empty"), fontSize = 13.sp, color = TextMuted)
                list?.forEach { user ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier.size(34.dp).background(PrimaryLight, RoundedCornerShape(17.dp)),
                            contentAlignment = Alignment.Center
                        ) { Text(user.name.take(1).uppercase(), color = Primary, fontWeight = FontWeight.Bold) }
                        Spacer(Modifier.width(10.dp))
                        Text(user.name, modifier = Modifier.weight(1f), color = TextPrimary)
                        OutlinedButton(onClick = { viewModel.unblock(user) }) { Text(t("mes_donnees.unblock")) }
                    }
                }
            }

            SectionCard {
                Text(t("mes_donnees.export_title"), fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(t("mes_donnees.export_intro"), fontSize = 12.sp, color = TextMuted)
                Button(
                    onClick = { viewModel.prepareExport { saveLauncher.launch("souqmar24-my-data.json") } },
                    enabled = !viewModel.exporting,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) { Text(t("mes_donnees.export_btn")) }
            }
        }
    }
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().background(WhiteColor, RoundedCornerShape(14.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(14.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

@Composable
private fun PrefRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(label, modifier = Modifier.weight(1f), fontSize = 14.sp, color = TextPrimary)
        Switch(
            checked = checked, onCheckedChange = onChange,
            colors = SwitchDefaults.colors(checkedTrackColor = Primary)
        )
    }
}
