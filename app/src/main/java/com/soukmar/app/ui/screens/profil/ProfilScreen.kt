@file:OptIn(ExperimentalMaterial3Api::class)

package com.soukmar.app.ui.screens.profil

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.soukmar.app.ui.components.ErrorBanner
import com.soukmar.app.ui.components.SuccessBanner
import com.soukmar.app.ui.theme.BorderColor
import com.soukmar.app.ui.theme.Gold
import com.soukmar.app.ui.theme.GoldLight
import com.soukmar.app.ui.theme.Primary
import com.soukmar.app.ui.theme.TextMuted
import com.soukmar.app.ui.theme.TextPrimary
import com.soukmar.app.ui.theme.WhiteColor
import com.soukmar.app.ui.i18n.t
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

@Composable
fun ProfilScreen(
    onBack: () -> Unit,
    viewModel: ProfilViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) { viewModel.load() }

    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.pickAvatar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour") } }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                viewModel.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
                viewModel.loadError || viewModel.profile == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Impossible de charger le profil.", color = TextMuted)
                }
                else -> ProfilContent(viewModel, onPickAvatar = { avatarPicker.launch("image/*") })
            }
        }
    }
}

@Composable
private fun ProfilContent(viewModel: ProfilViewModel, onPickAvatar: () -> Unit) {
    val profile = viewModel.profile!!

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        // Avatar + identity card
        Column(
            modifier = Modifier.fillMaxWidth().background(WhiteColor, RoundedCornerShape(14.dp)).border(1.dp, BorderColor, RoundedCornerShape(14.dp)).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                Box(
                    modifier = Modifier.size(84.dp).clip(CircleShape).background(Primary),
                    contentAlignment = Alignment.Center
                ) {
                    if (profile.image != null) {
                        AsyncImage(model = profile.image, contentDescription = profile.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else {
                        Text(profile.name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 32.sp)
                    }
                    if (viewModel.uploadingImage) {
                        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        }
                    }
                }
                IconButton(
                    onClick = onPickAvatar,
                    enabled = !viewModel.uploadingImage,
                    modifier = Modifier.align(Alignment.BottomEnd).size(28.dp).background(Primary, CircleShape).border(2.dp, WhiteColor, CircleShape)
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = "Changer la photo", tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(profile.name, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimary)
            Text(profile.email, color = TextMuted, fontSize = 13.sp)
            memberSince(profile.createdAt)?.let {
                Spacer(Modifier.height(4.dp))
                Text("${t("profil.member_since")} $it", color = TextMuted, fontSize = 12.sp)
            }
            if (profile.role == "ADMIN") {
                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier.background(GoldLight, RoundedCornerShape(999.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                    Text(t("profil.role_admin"), color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Edit profile form
        Column(
            modifier = Modifier.fillMaxWidth().background(WhiteColor, RoundedCornerShape(14.dp)).border(1.dp, BorderColor, RoundedCornerShape(14.dp)).padding(16.dp)
        ) {
            Text(t("profil.edit_title"), fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(12.dp))
            viewModel.successMessage?.let { SuccessBanner(it); Spacer(Modifier.height(10.dp)) }
            viewModel.errorMessage?.let { ErrorBanner(it); Spacer(Modifier.height(10.dp)) }

            OutlinedTextField(
                value = viewModel.name,
                onValueChange = { viewModel.name = it },
                label = { Text(t("profil.name")) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, cursorColor = Primary)
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = viewModel.profile?.email ?: "",
                onValueChange = {},
                label = { Text("Email") },
                enabled = false,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            Text(t("profil.phone"), style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            com.soukmar.app.ui.components.PhoneInputField(value = viewModel.phone, onValueChange = { viewModel.phone = it })
            if (!viewModel.profile?.phone.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                PhoneVerificationRow(viewModel)
            }
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = viewModel.city,
                onValueChange = { viewModel.city = it },
                label = { Text(t("profil.city")) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, cursorColor = Primary)
            )
            Spacer(Modifier.height(14.dp))
            Text(t("auth.account_type"), style = MaterialTheme.typography.labelLarge, color = TextPrimary)
            Spacer(Modifier.height(6.dp))
            com.soukmar.app.ui.components.AccountTypeSelector(
                selected = viewModel.accountType,
                onSelect = { viewModel.accountType = it },
                options = listOf("PRIVATE" to t("auth.account_type_private"), "BUSINESS" to t("auth.account_type_business"))
            )
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = { viewModel.saveProfile() },
                enabled = !viewModel.saving,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                if (viewModel.saving) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(t("profil.save"))
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Change password form
        Column(
            modifier = Modifier.fillMaxWidth().background(WhiteColor, RoundedCornerShape(14.dp)).border(1.dp, BorderColor, RoundedCornerShape(14.dp)).padding(16.dp)
        ) {
            Text(t("profil.change_password_title"), fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(12.dp))
            viewModel.pwSuccessMessage?.let { SuccessBanner(it); Spacer(Modifier.height(10.dp)) }
            viewModel.pwErrorMessage?.let { ErrorBanner(it); Spacer(Modifier.height(10.dp)) }

            OutlinedTextField(
                value = viewModel.currentPassword,
                onValueChange = { viewModel.currentPassword = it },
                label = { Text(t("profil.current_password")) },
                singleLine = true,
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, cursorColor = Primary)
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = viewModel.newPassword,
                onValueChange = { viewModel.newPassword = it },
                label = { Text(t("profil.new_password")) },
                singleLine = true,
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, cursorColor = Primary)
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = viewModel.confirmPassword,
                onValueChange = { viewModel.confirmPassword = it },
                label = { Text(t("profil.confirm_password")) },
                singleLine = true,
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, cursorColor = Primary)
            )
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = { viewModel.changePassword() },
                enabled = !viewModel.pwSaving,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                if (viewModel.pwSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(t("profil.change_password_btn"))
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun PhoneVerificationRow(viewModel: ProfilViewModel) {
    Column {
        when {
            viewModel.profile?.phoneVerified == true -> {
                Text("✓ ${t("profil.phone_verified")}", color = com.soukmar.app.ui.theme.SuccessColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            !viewModel.phoneCodeSent -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(t("profil.phone_not_verified"), color = TextMuted, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    TextButton(onClick = { viewModel.sendPhoneCode() }, enabled = !viewModel.phoneSendingCode) {
                        Text(if (viewModel.phoneSendingCode) t("profil.phone_sending") else t("profil.phone_verify_btn"), fontSize = 12.sp)
                    }
                }
            }
            else -> {
                Text(t("profil.phone_code_hint"), color = TextMuted, fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = viewModel.phoneCode,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) viewModel.phoneCode = it },
                        placeholder = { Text("000000") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, cursorColor = Primary)
                    )
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = { viewModel.verifyPhoneCode() }, enabled = !viewModel.phoneVerifying && viewModel.phoneCode.isNotBlank()) {
                        Text(if (viewModel.phoneVerifying) t("profil.phone_verifying") else t("profil.phone_confirm_btn"), fontSize = 12.sp)
                    }
                }
                TextButton(onClick = { viewModel.sendPhoneCode() }, enabled = !viewModel.phoneSendingCode) {
                    Text(t("profil.phone_resend"), fontSize = 12.sp)
                }
            }
        }
        viewModel.phoneMessage?.let {
            Spacer(Modifier.height(4.dp))
            SuccessBanner(it)
        }
        viewModel.phoneErrorMessage?.let {
            Spacer(Modifier.height(4.dp))
            ErrorBanner(it)
        }
    }
}

private val memberSinceFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH).withZone(ZoneId.systemDefault())

private fun memberSince(iso: String?): String? {
    if (iso == null) return null
    return try {
        memberSinceFormatter.format(Instant.parse(iso))
    } catch (e: DateTimeParseException) {
        null
    }
}
