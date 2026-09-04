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
import com.soukmar.app.ui.i18n.t
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
            Text(t("auth.reset_invalid_title"), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text(t("auth.reset_invalid_sub"))
            Spacer(Modifier.height(16.dp))
            PrimaryButton(text = t("auth.forgot_title"), onClick = onRequestNewLink)
            return@Column
        }

        if (viewModel.success) {
            Text(t("auth.reset_title"), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            SuccessBanner(t("auth.reset_success"))
            LaunchedEffect(Unit) {
                delay(2000)
                onDone()
            }
            return@Column
        }

        Text(t("auth.reset_title"), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(4.dp))
        Text(t("auth.reset_sub"))
        Spacer(Modifier.height(24.dp))

        viewModel.error?.let { ErrorBanner(it); Spacer(Modifier.height(12.dp)) }

        AppTextField(value = viewModel.password, onValueChange = { viewModel.password = it }, label = t("auth.reset_new_password"), isPassword = true)
        Spacer(Modifier.height(12.dp))
        AppTextField(value = viewModel.confirmPassword, onValueChange = { viewModel.confirmPassword = it }, label = t("auth.reset_confirm_password"), isPassword = true)
        Spacer(Modifier.height(20.dp))
        PrimaryButton(text = t("auth.reset_btn"), onClick = { viewModel.submit(token) }, loading = viewModel.loading)
    }
}
