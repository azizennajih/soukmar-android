package com.soukmar.app.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.soukmar.app.ui.components.*
import com.soukmar.app.ui.theme.Primary
import com.soukmar.app.ui.theme.TextMuted

@Composable
fun ForgotPasswordScreen(
    onBackToLogin: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SoukMarLogo()
        Spacer(Modifier.height(24.dp))
        Text("Mot de passe oublié ?", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(4.dp))
        Text("Entrez votre email, nous vous enverrons un lien de réinitialisation.", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))

        if (viewModel.sent) {
            SuccessBanner("Si un compte existe pour ${viewModel.email}, un email avec un lien de réinitialisation vient d'être envoyé.")
            Spacer(Modifier.height(16.dp))
            PrimaryButton(text = "Retour à la connexion", onClick = onBackToLogin)
        } else {
            viewModel.error?.let { ErrorBanner(it); Spacer(Modifier.height(12.dp)) }
            AppTextField(value = viewModel.email, onValueChange = { viewModel.email = it }, label = "Email", keyboardType = KeyboardType.Email)
            Spacer(Modifier.height(16.dp))
            PrimaryButton(text = "Envoyer le lien", onClick = { viewModel.submit() }, loading = viewModel.loading)
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onBackToLogin) { Text("Retour à la connexion", color = Primary) }
        }
    }
}
