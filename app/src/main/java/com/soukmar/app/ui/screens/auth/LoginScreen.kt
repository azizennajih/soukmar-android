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
import com.soukmar.app.ui.components.AppTextField
import com.soukmar.app.ui.components.ErrorBanner
import com.soukmar.app.ui.components.PrimaryButton
import com.soukmar.app.ui.components.SuccessBanner
import com.soukmar.app.ui.theme.Primary
import com.soukmar.app.ui.theme.TextMuted

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        com.soukmar.app.ui.components.SoukMarLogo()
        Spacer(Modifier.height(24.dp))
        Text("Bon retour !", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(4.dp))
        Text("Connectez-vous à votre compte", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))

        viewModel.error?.let {
            ErrorBanner(it)
            Spacer(Modifier.height(12.dp))
        }

        viewModel.unverifiedEmail?.let {
            if (viewModel.resendOk) {
                SuccessBanner("Email renvoyé !")
            } else {
                TextButton(onClick = { viewModel.resendVerification() }, enabled = !viewModel.resendLoading) {
                    Text(if (viewModel.resendLoading) "Envoi..." else "Renvoyer l'email de confirmation")
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        AppTextField(
            value = viewModel.email,
            onValueChange = { viewModel.email = it },
            label = "Email",
            keyboardType = KeyboardType.Email
        )
        Spacer(Modifier.height(12.dp))
        AppTextField(
            value = viewModel.password,
            onValueChange = { viewModel.password = it },
            label = "Mot de passe",
            isPassword = true
        )
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onNavigateToForgotPassword) {
                Text("Mot de passe oublié ?", color = Primary)
            }
        }
        Spacer(Modifier.height(8.dp))
        PrimaryButton(
            text = "Se connecter",
            onClick = { viewModel.submit(onLoginSuccess) },
            loading = viewModel.loading
        )
        Spacer(Modifier.height(20.dp))
        Row {
            Text("Pas encore de compte ? ", color = TextMuted)
            TextButton(onClick = onNavigateToRegister, contentPadding = PaddingValues(0.dp)) {
                Text("S'inscrire gratuitement", color = Primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}
