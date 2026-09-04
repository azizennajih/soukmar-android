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
import com.soukmar.app.ui.components.LanguageSwitcher
import com.soukmar.app.ui.components.PrimaryButton
import com.soukmar.app.ui.components.SuccessBanner
import com.soukmar.app.ui.i18n.t
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
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            LanguageSwitcher()
        }
        Spacer(Modifier.height(8.dp))
        com.soukmar.app.ui.components.SoukMarLogo()
        Spacer(Modifier.height(24.dp))
        Text(t("auth.login_title"), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(4.dp))
        Text(t("auth.login_sub"), color = TextMuted, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))

        viewModel.error?.let {
            ErrorBanner(it)
            Spacer(Modifier.height(12.dp))
        }

        viewModel.unverifiedEmail?.let {
            if (viewModel.resendOk) {
                SuccessBanner(t("auth.verify_resend_ok"))
            } else {
                TextButton(onClick = { viewModel.resendVerification() }, enabled = !viewModel.resendLoading) {
                    Text(if (viewModel.resendLoading) "…" else t("auth.unverified_resend"))
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        AppTextField(
            value = viewModel.email,
            onValueChange = { viewModel.email = it },
            label = t("auth.email"),
            keyboardType = KeyboardType.Email
        )
        Spacer(Modifier.height(12.dp))
        AppTextField(
            value = viewModel.password,
            onValueChange = { viewModel.password = it },
            label = t("auth.password"),
            isPassword = true
        )
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onNavigateToForgotPassword) {
                Text(t("auth.forgot"), color = Primary)
            }
        }
        Spacer(Modifier.height(8.dp))
        PrimaryButton(
            text = t("auth.login_btn"),
            onClick = { viewModel.submit(onLoginSuccess) },
            loading = viewModel.loading
        )
        Spacer(Modifier.height(20.dp))
        Row {
            Text("${t("auth.no_account")} ", color = TextMuted)
            TextButton(onClick = onNavigateToRegister, contentPadding = PaddingValues(0.dp)) {
                Text(t("auth.register_link"), color = Primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}
