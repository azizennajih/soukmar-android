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
import com.soukmar.app.ui.i18n.t
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
        Text(t("auth.forgot_title"), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(4.dp))
        Text(t("auth.forgot_sub"), color = TextMuted, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))

        if (viewModel.sent) {
            SuccessBanner(t("auth.forgot_sent", "email" to viewModel.email))
            Spacer(Modifier.height(16.dp))
            PrimaryButton(text = t("auth.back_to_login"), onClick = onBackToLogin)
        } else {
            viewModel.error?.let { ErrorBanner(it); Spacer(Modifier.height(12.dp)) }
            AppTextField(value = viewModel.email, onValueChange = { viewModel.email = it }, label = t("auth.email"), keyboardType = KeyboardType.Email)
            Spacer(Modifier.height(16.dp))
            PrimaryButton(text = t("auth.forgot_btn"), onClick = { viewModel.submit() }, loading = viewModel.loading)
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onBackToLogin) { Text(t("auth.back_to_login"), color = Primary) }
        }
    }
}
