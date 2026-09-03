package com.soukmar.app.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.soukmar.app.ui.components.*
import kotlinx.coroutines.delay

@Composable
fun ResetPasswordScreen(
    token: String?,
    onDone: () -> Unit,
    onRequestNewLink: () -> Unit,
    viewModel: ResetPasswordViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SoukMarLogo()
        Spacer(Modifier.height(24.dp))

        if (token.isNullOrBlank()) {
            Text("Lien invalide", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text("Ce lien de réinitialisation est invalide ou a expiré. Demandez-en un nouveau.")
            Spacer(Modifier.height(16.dp))
            PrimaryButton(text = "Mot de passe oublié", onClick = onRequestNewLink)
            return@Column
        }

        if (viewModel.success) {
            Text("Réinitialiser le mot de passe", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            SuccessBanner("Mot de passe réinitialisé ! Redirection vers la connexion...")
            LaunchedEffect(Unit) {
                delay(2000)
                onDone()
            }
            return@Column
        }

        Text("Réinitialiser le mot de passe", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(4.dp))
        Text("Choisissez un nouveau mot de passe.")
        Spacer(Modifier.height(24.dp))

        viewModel.error?.let { ErrorBanner(it); Spacer(Modifier.height(12.dp)) }

        AppTextField(value = viewModel.password, onValueChange = { viewModel.password = it }, label = "Nouveau mot de passe", isPassword = true)
        Spacer(Modifier.height(12.dp))
        AppTextField(value = viewModel.confirmPassword, onValueChange = { viewModel.confirmPassword = it }, label = "Confirmer le mot de passe", isPassword = true)
        Spacer(Modifier.height(20.dp))
        PrimaryButton(text = "Réinitialiser le mot de passe", onClick = { viewModel.submit(token) }, loading = viewModel.loading)
    }
}
