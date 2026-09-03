package com.soukmar.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.soukmar.app.ui.components.OutlineButtonSoukMar
import com.soukmar.app.ui.components.SoukMarLogo
import com.soukmar.app.ui.theme.TextMuted

/** Post-login landing screen. Listing browse/search, chat, deposer-annonce
 * etc. land here in the next phases — this confirms the auth pipeline works
 * end-to-end against the real backend first. */
@Composable
fun HomeScreen(
    onLoggedOut: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        SoukMarLogo()
        Spacer(Modifier.height(24.dp))
        if (viewModel.loading) {
            CircularProgressIndicator()
        } else {
            viewModel.user?.let { u ->
                Text("Bonjour, ${u.name} 👋", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(4.dp))
                Text(u.email, color = TextMuted)
            } ?: Text("Session expirée.", color = TextMuted)
            Spacer(Modifier.height(32.dp))
            Text("Recherche, favoris, chat et bien plus arrivent dans les prochaines étapes.", color = TextMuted, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(24.dp))
            OutlineButtonSoukMar(text = "Se déconnecter", onClick = { viewModel.logout(onLoggedOut) })
        }
    }
}
