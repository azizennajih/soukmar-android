@file:OptIn(ExperimentalMaterial3Api::class)

package com.soukmar.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.soukmar.app.ui.components.LanguageSwitcher
import com.soukmar.app.ui.i18n.t
import com.soukmar.app.ui.theme.BorderColor
import com.soukmar.app.ui.theme.ErrorColor
import com.soukmar.app.ui.theme.Primary
import com.soukmar.app.ui.theme.TextMuted
import com.soukmar.app.ui.theme.TextPrimary
import com.soukmar.app.ui.theme.WhiteColor

/** Consolidates entry points that used to be scattered across HomeScreen's
 * ⋮ overflow menu (language, legal, logout) plus the new account-deletion
 * flow, mirroring the web's /parametres hub — minus the Premium/Aide pages,
 * which are out of scope here (marketing/payment content, not app parity). */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenProfil: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenLegal: () -> Unit,
    onOpenDeleteAccount: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(t("parametres.title")) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = t("common.cancel")) } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(t("nav.settings"), color = TextMuted, fontSize = 12.sp)
                LanguageSwitcher()
            }
            Spacer(Modifier.height(8.dp))

            SettingsRow(Icons.Filled.Person, t("parametres.profile_account"), t("parametres.profile_account_sub"), onOpenProfil)
            SettingsRow(Icons.Filled.NotificationsNone, t("parametres.notifications"), t("parametres.notifications_sub"), onOpenNotifications)
            SettingsRow(Icons.Filled.Gavel, t("parametres.legal"), t("parametres.legal_sub"), onOpenLegal)

            Spacer(Modifier.height(20.dp))
            SettingsRow(
                Icons.Filled.DeleteForever,
                t("parametres.delete_account"),
                t("parametres.delete_account_sub"),
                onOpenDeleteAccount,
                danger = true
            )

            Spacer(Modifier.height(20.dp))
            OutlinedButton(
                onClick = { viewModel.logout(onLoggedOut) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(t("nav.logout"))
            }
        }
    }
}

@Composable
private fun SettingsRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit, danger: Boolean = false) {
    val tint = if (danger) ErrorColor else Primary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(WhiteColor, RoundedCornerShape(14.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(tint.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = if (danger) ErrorColor else TextPrimary)
            Text(subtitle, color = TextMuted, fontSize = 12.sp)
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextMuted)
    }
    Spacer(Modifier.height(10.dp))
}
