package com.soukmar.app.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.soukmar.app.ui.components.*
import com.soukmar.app.ui.theme.Primary
import com.soukmar.app.ui.theme.TextMuted

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SoukMarLogo()
        Spacer(Modifier.height(24.dp))

        if (viewModel.emailSent) {
            Text("Vérifiez votre email", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                "Un lien de confirmation a été envoyé à ${viewModel.registeredEmail}. Cliquez sur le lien pour activer votre compte.",
                color = TextMuted, style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(16.dp))
            if (viewModel.resendOk) {
                SuccessBanner("Email renvoyé !")
            } else {
                OutlineButtonSoukMar(
                    text = if (viewModel.resendLoading) "Envoi..." else "Renvoyer l'email",
                    onClick = { viewModel.resend() },
                    enabled = !viewModel.resendLoading
                )
            }
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onNavigateToLogin) { Text("Retour à la connexion", color = Primary) }
            return@Column
        }

        Text("Créer un compte", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(4.dp))
        Text("Rejoignez des milliers d'acheteurs et vendeurs", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))

        viewModel.error?.let {
            ErrorBanner(it)
            Spacer(Modifier.height(12.dp))
        }

        AppTextField(value = viewModel.name, onValueChange = { viewModel.name = it }, label = "Nom complet")
        Spacer(Modifier.height(12.dp))
        AppTextField(value = viewModel.email, onValueChange = { viewModel.email = it }, label = "Email", keyboardType = KeyboardType.Email)
        Spacer(Modifier.height(12.dp))
        AppTextField(value = viewModel.phone, onValueChange = { viewModel.phone = it }, label = "Téléphone", keyboardType = KeyboardType.Phone)
        Spacer(Modifier.height(12.dp))
        AppTextField(value = viewModel.city, onValueChange = { viewModel.city = it }, label = "Ville")
        Spacer(Modifier.height(12.dp))
        AppTextField(value = viewModel.password, onValueChange = { viewModel.password = it }, label = "Mot de passe", isPassword = true)
        Spacer(Modifier.height(12.dp))
        AppTextField(value = viewModel.confirmPassword, onValueChange = { viewModel.confirmPassword = it }, label = "Confirmer le mot de passe", isPassword = true)
        Spacer(Modifier.height(20.dp))
        PrimaryButton(text = "Créer mon compte", onClick = { viewModel.submit() }, loading = viewModel.loading)
        Spacer(Modifier.height(20.dp))
        Row {
            Text("Déjà un compte ? ", color = TextMuted)
            TextButton(onClick = onNavigateToLogin, contentPadding = PaddingValues(0.dp)) {
                Text("Se connecter", color = Primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}
