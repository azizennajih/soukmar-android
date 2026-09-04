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
import com.soukmar.app.ui.i18n.t
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
            Text(t("auth.verify_email_title"), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                "${t("auth.verify_email_sub")} ${viewModel.registeredEmail}. ${t("auth.verify_email_hint")}",
                color = TextMuted, style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(16.dp))
            if (viewModel.resendOk) {
                SuccessBanner(t("auth.verify_resend_ok"))
            } else {
                OutlineButtonSoukMar(
                    text = if (viewModel.resendLoading) "Envoi..." else t("auth.verify_resend"),
                    onClick = { viewModel.resend() },
                    enabled = !viewModel.resendLoading
                )
            }
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onNavigateToLogin) { Text(t("auth.back_to_login"), color = Primary) }
            return@Column
        }

        Text(t("auth.register_title"), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(4.dp))
        Text(t("auth.register_sub"), color = TextMuted, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))

        viewModel.error?.let {
            ErrorBanner(it)
            Spacer(Modifier.height(12.dp))
        }

        AppTextField(value = viewModel.name, onValueChange = { viewModel.name = it }, label = t("auth.name"))
        Spacer(Modifier.height(12.dp))
        AppTextField(value = viewModel.email, onValueChange = { viewModel.email = it }, label = t("auth.email"), keyboardType = KeyboardType.Email)
        Spacer(Modifier.height(12.dp))
        AppTextField(value = viewModel.phone, onValueChange = { viewModel.phone = it }, label = t("auth.phone"), keyboardType = KeyboardType.Phone)
        Spacer(Modifier.height(12.dp))
        AppTextField(value = viewModel.city, onValueChange = { viewModel.city = it }, label = t("auth.city"))
        Spacer(Modifier.height(12.dp))
        AppTextField(value = viewModel.password, onValueChange = { viewModel.password = it }, label = t("auth.password"), isPassword = true)
        Spacer(Modifier.height(12.dp))
        AppTextField(value = viewModel.confirmPassword, onValueChange = { viewModel.confirmPassword = it }, label = t("auth.reset_confirm_password"), isPassword = true)
        Spacer(Modifier.height(20.dp))
        PrimaryButton(text = t("auth.register_btn"), onClick = { viewModel.submit() }, loading = viewModel.loading)
        Spacer(Modifier.height(20.dp))
        Row {
            Text("${t("auth.has_account")} ", color = TextMuted)
            TextButton(onClick = onNavigateToLogin, contentPadding = PaddingValues(0.dp)) {
                Text(t("auth.login_link"), color = Primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}
