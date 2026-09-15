@file:OptIn(ExperimentalMaterial3Api::class)

package com.soukmar.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.soukmar.app.ui.components.AppTextField
import com.soukmar.app.ui.components.ErrorBanner
import com.soukmar.app.ui.components.SuccessBanner
import com.soukmar.app.ui.i18n.t
import com.soukmar.app.ui.theme.BorderColor
import com.soukmar.app.ui.theme.ErrorColor
import com.soukmar.app.ui.theme.TextPrimary
import com.soukmar.app.ui.theme.WhiteColor

@Composable
fun DeleteAccountScreen(
    onBack: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: DeleteAccountViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(t("delete_account.title")) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = t("common.cancel")) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            if (viewModel.deleted) {
                SuccessBanner(t("delete_account.success"))
                return@Column
            }

            Box(
                modifier = Modifier.fillMaxWidth().background(ErrorColor.copy(alpha = 0.08f), RoundedCornerShape(14.dp)).padding(16.dp)
            ) {
                Text(t("delete_account.warning"), color = TextPrimary)
            }

            Spacer(Modifier.height(20.dp))

            Column(
                modifier = Modifier.fillMaxWidth().background(WhiteColor, RoundedCornerShape(14.dp)).border(1.dp, BorderColor, RoundedCornerShape(14.dp)).padding(16.dp)
            ) {
                viewModel.errorMessage?.let { ErrorBanner(it); Spacer(Modifier.height(10.dp)) }

                AppTextField(
                    value = viewModel.password,
                    onValueChange = { viewModel.password = it },
                    label = t("delete_account.password_label"),
                    isPassword = true
                )
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = viewModel.confirmed,
                        onCheckedChange = { viewModel.confirmed = it },
                        colors = CheckboxDefaults.colors(checkedColor = ErrorColor)
                    )
                    Text(t("delete_account.confirm_checkbox"), modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.submit(onLoggedOut) },
                    enabled = viewModel.canSubmit,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorColor, contentColor = Color.White)
                ) {
                    if (viewModel.submitting) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text(t("delete_account.submit_btn"), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
