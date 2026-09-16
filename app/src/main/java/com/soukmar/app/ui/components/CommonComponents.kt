package com.soukmar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soukmar.app.ui.i18n.t
import com.soukmar.app.ui.theme.ErrorColor
import com.soukmar.app.ui.theme.Primary
import com.soukmar.app.ui.theme.PrimaryLight
import com.soukmar.app.ui.theme.SuccessColor
import com.soukmar.app.ui.theme.TextMuted
import com.soukmar.app.ui.theme.TextPrimary

@Composable
fun SoukMarLogo(modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Primary, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("S", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
        }
        Spacer(Modifier.width(8.dp))
        Row {
            Text("Souk", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 20.sp)
            Text("Mar", color = Primary, fontWeight = FontWeight.Black, fontSize = 20.sp)
        }
    }
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true,
    singleLine: Boolean = true,
) {
    var passwordVisible by remember { mutableStateOf(false) }
    if (isPassword) {
        // Mirrors the web app's password-input component: revealing the value
        // auto-hides it again after a few seconds instead of staying in
        // plaintext indefinitely.
        LaunchedEffect(passwordVisible) {
            if (passwordVisible) {
                kotlinx.coroutines.delay(8000)
                passwordVisible = false
            }
        }
    }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = singleLine,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = null
                    )
                }
            }
        } else null,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Primary,
            cursorColor = Primary,
        )
    )
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(50.dp),
        enabled = enabled && !loading,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.White)
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun OutlineButtonSoukMar(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(50.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/** Two-way Privat/Gewerblich picker, mirrors the web's `account-type-choice`
 * pill row (register + profil forms). [options] is (value, label) pairs. */
@Composable
fun AccountTypeSelector(
    selected: String,
    onSelect: (String) -> Unit,
    options: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        options.forEach { (value, label) ->
            val isSelected = selected == value
            OutlinedButton(
                onClick = { onSelect(value) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = if (isSelected) {
                    ButtonDefaults.outlinedButtonColors(containerColor = PrimaryLight, contentColor = Primary)
                } else {
                    ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                },
                border = androidx.compose.foundation.BorderStroke(1.5.dp, if (isSelected) Primary else Color(0xFFE5E9EE)),
            ) {
                Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}

/** Trust-signal badge mirroring the web's `app-verified-badge`: renders
 * nothing if neither flag is set, so an unverified account just shows no
 * badge rather than a warning. */
@Composable
fun VerifiedBadge(emailVerified: Boolean, phoneVerified: Boolean, modifier: Modifier = Modifier) {
    if (!emailVerified && !phoneVerified) return
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        if (emailVerified) VerifiedPill(com.soukmar.app.ui.i18n.t("seller.email_verified_short"))
        if (phoneVerified) VerifiedPill(com.soukmar.app.ui.i18n.t("seller.phone_verified_short"))
    }
}

@Composable
private fun VerifiedPill(label: String) {
    Row(
        modifier = Modifier
            .background(SuccessColor.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("✓ $label", color = SuccessColor, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ErrorBanner(message: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(ErrorColor.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Text(message, color = ErrorColor, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun SuccessBanner(message: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(SuccessColor.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Text("✅ $message", color = SuccessColor, style = MaterialTheme.typography.bodyMedium)
    }
}

/** Small icon+label row for a seller's account type (Privat/Gewerblich),
 * mirrors the web's business/private icon+text pairing on the seller
 * profile and listing detail pages. Renders nothing for a null type. */
@Composable
fun AccountTypeLabel(accountType: String?, modifier: Modifier = Modifier, fontSize: androidx.compose.ui.unit.TextUnit = 12.sp) {
    if (accountType == null) return
    val isBusiness = accountType == "BUSINESS"
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(
            imageVector = if (isBusiness) Icons.Filled.Business else Icons.Filled.Person,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(13.dp)
        )
        Text(
            t(if (isBusiness) "auth.account_type_business" else "auth.account_type_private"),
            color = TextMuted,
            fontSize = fontSize
        )
    }
}
