@file:OptIn(ExperimentalMaterial3Api::class)

package com.soukmar.app.ui.screens.profil

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
            OutlinedTextField(
                value = viewModel.phone,
                onValueChange = { viewModel.phone = it },
                label = { Text(t("profil.phone")) },
                placeholder = { Text("+212 6 00 00 00 00") },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, cursorColor = Primary)
            )
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

private val memberSinceFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH).withZone(ZoneId.systemDefault())

private fun memberSince(iso: String?): String? {
    if (iso == null) return null
    return try {
        memberSinceFormatter.format(Instant.parse(iso))
    } catch (e: DateTimeParseException) {
        null
    }
}
