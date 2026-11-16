package com.soukmar.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soukmar.app.ui.i18n.t
import com.soukmar.app.ui.theme.Accent
import com.soukmar.app.ui.theme.BorderColor
import com.soukmar.app.ui.theme.ControlHeight
import com.soukmar.app.ui.theme.ErrorColor
import com.soukmar.app.ui.theme.ErrorDark
import com.soukmar.app.ui.theme.ErrorLight
import com.soukmar.app.ui.theme.Gold
import com.soukmar.app.ui.theme.GoldLight
import com.soukmar.app.ui.theme.IconSize
import com.soukmar.app.ui.theme.Primary
import com.soukmar.app.ui.theme.PrimaryLight
import com.soukmar.app.ui.theme.ShapeBadge
import com.soukmar.app.ui.theme.ShapeCard
import com.soukmar.app.ui.theme.ShapeControl
import com.soukmar.app.ui.theme.ShapePill
import com.soukmar.app.ui.theme.Spacing
import com.soukmar.app.ui.theme.SuccessColor
import com.soukmar.app.ui.theme.SuccessDark
import com.soukmar.app.ui.theme.SuccessLight
import com.soukmar.app.ui.theme.SurfaceMuted
import com.soukmar.app.ui.theme.TextMuted
import com.soukmar.app.ui.theme.TextPrimary
import com.soukmar.app.ui.theme.TextSubtle
import com.soukmar.app.ui.theme.WhiteColor

/** The brand mark: red rounded tile with the white S of the SouqMar24 logo (same path as the launcher icon and the web favicon,
 * 64-unit grid, stroke 8, round caps). */
@Composable
fun SoukMarMark(size: Dp = 32.dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size).clip(RoundedCornerShape(size * 0.28f)).background(Primary)) {
        scale(this.size.width / 64f, this.size.height / 64f, pivot = Offset.Zero) {
            drawPath(
                PathParser().parsePathString("M43,21.5C41.5,15.5 22,14.5 22,25.5c0,10.5 21,5.5 21,15.5 0,11 -19.5,10 -22,3").toPath(),
                color = WhiteColor,
                style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

@Composable
fun SoukMarLogo(modifier: Modifier = Modifier, markSize: Dp = 32.dp, fontSize: TextUnit = 20.sp) {
    // The wordmark is a brand: it always reads "S SouqMar24" left to right, also in RTL languages.
    CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) {
        Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
            SoukMarMark(size = markSize)
            Spacer(Modifier.width(markSize * 0.25f))
            Text("SouqMar", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = fontSize, letterSpacing = (-0.3).sp)
            Text("24", color = Primary, fontWeight = FontWeight.Black, fontSize = fontSize, letterSpacing = (-0.3).sp)
        }
    }
}

/** Top bar of every pushed screen: white, title on the start side, one back arrow (mirrored in RTL). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoukTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = { Text(title, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = t("common.close"))
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = WhiteColor,
            scrolledContainerColor = WhiteColor,
            titleContentColor = TextPrimary,
            navigationIconContentColor = TextPrimary,
            actionIconContentColor = TextPrimary,
        )
    )
}

/** The search entry of the home screen: one large pill, the whole surface is the touch target; photo search on the end side. */
@Composable
fun SearchPill(
    placeholder: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onCameraClick: (() -> Unit)? = null,
    cameraLabel: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(ControlHeight)
            .clip(ShapePill)
            .background(SurfaceMuted)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = Spacing.lg, end = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.Search, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(IconSize.standard))
        Spacer(Modifier.width(Spacing.md))
        Text(placeholder, color = TextMuted, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        if (onCameraClick != null) {
            IconButton(onClick = onCameraClick) {
                Icon(Icons.Outlined.CameraAlt, contentDescription = cameraLabel, tint = TextPrimary, modifier = Modifier.size(IconSize.standard))
            }
        }
    }
}

/** Section title with an optional "see all" action on the end side. */
@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    Row(modifier = modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = TextPrimary, modifier = Modifier.weight(1f))
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) { Text(actionLabel, style = MaterialTheme.typography.labelLarge, color = Primary) }
        }
    }
}

/** The calm icon of every empty state: 32-dp outline icon in a 72-dp tinted circle. */
@Composable
fun EmptyIcon(icon: ImageVector, modifier: Modifier = Modifier) {
    Box(modifier.size(72.dp).background(SurfaceMuted, CircleShape), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = TextMuted, modifier = Modifier.size(32.dp))
    }
}

/** Empty or error state of a list: calm icon in a tinted circle, one sentence, at most one action. */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EmptyIcon(icon)
        Spacer(Modifier.height(Spacing.lg))
        Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, textAlign = TextAlign.Center)
        if (subtitle != null) {
            Spacer(Modifier.height(Spacing.xs))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextMuted, textAlign = TextAlign.Center)
        }
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(Spacing.lg))
            Button(onClick = onAction, modifier = Modifier.heightIn(min = 48.dp)) { Text(actionLabel) }
        }
    }
}

/** Small label on images and in lists. The yellow accent is reserved for offers, boosts and premium. */
@Composable
fun SoukBadge(
    text: String,
    modifier: Modifier = Modifier,
    container: Color = SurfaceMuted,
    content: Color = TextPrimary,
    icon: ImageVector? = null,
) {
    Row(
        modifier = modifier.background(container, ShapeBadge).padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(4.dp))
        }
        Text(text, color = content, style = MaterialTheme.typography.labelSmall, maxLines = 1)
    }
}

/** The standard card surface: white, 16-dp corners, hairline border, no shadow. */
fun Modifier.soukCard(): Modifier = this.clip(ShapeCard).background(WhiteColor).border(1.dp, BorderColor, ShapeCard)

/** One row of a menu list (account, settings): icon, title, optional subtitle, chevron (mirrored in RTL). */
@Composable
fun MenuRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    tint: Color = TextPrimary,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .heightIn(min = 56.dp)
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(IconSize.standard))
        Spacer(Modifier.width(Spacing.lg))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = tint)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextMuted)
        }
        if (trailing != null) {
            trailing()
        } else {
            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = TextSubtle, modifier = Modifier.size(IconSize.standard))
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
        shape = ShapeControl,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = t(if (passwordVisible) "common.hide_password" else "common.show_password")
                    )
                }
            }
        } else null,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Primary,
            focusedLabelColor = Primary,
            cursorColor = Primary,
            unfocusedBorderColor = BorderColor,
            unfocusedLabelColor = TextMuted,
        )
    )
}

/** The one primary action of a screen: red, full width, 52 dp. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(ControlHeight),
        enabled = enabled && !loading,
        shape = ShapePill,
        colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = WhiteColor)
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = WhiteColor, strokeWidth = 2.dp)
        } else {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(IconSize.small))
                Spacer(Modifier.width(Spacing.sm))
            }
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** Secondary action next to or below a primary one: white with a border, dark text. */
@Composable
fun OutlineButtonSoukMar(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(ControlHeight),
        enabled = enabled,
        shape = ShapePill,
        border = BorderStroke(1.dp, BorderColor),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(IconSize.small))
            Spacer(Modifier.width(Spacing.sm))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/** Yellow action for offers and boosts (dark text for contrast). */
@Composable
fun AccentButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(ControlHeight),
        enabled = enabled,
        shape = ShapePill,
        colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = TextPrimary)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(IconSize.small))
            Spacer(Modifier.width(Spacing.sm))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/** Hint in the accent yellow (tips, offers, things to know); not for errors. */
@Composable
fun HintBanner(message: String, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    Row(
        modifier = modifier.fillMaxWidth().background(GoldLight, ShapeControl).padding(Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        if (icon != null) Icon(icon, contentDescription = null, tint = Gold, modifier = Modifier.size(IconSize.small))
        Text(message, color = Gold, style = MaterialTheme.typography.bodyMedium)
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
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                shape = ShapeControl,
                colors = if (isSelected) {
                    ButtonDefaults.outlinedButtonColors(containerColor = PrimaryLight, contentColor = Primary)
                } else {
                    ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                },
                border = BorderStroke(1.5.dp, if (isSelected) Primary else BorderColor),
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
fun VerifiedBadge(emailVerified: Boolean, phoneVerified: Boolean, idVerified: Boolean = false, modifier: Modifier = Modifier) {
    if (!emailVerified && !phoneVerified && !idVerified) return
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        if (emailVerified) VerifiedPill(com.soukmar.app.ui.i18n.t("seller.email_verified_short"))
        if (phoneVerified) VerifiedPill(com.soukmar.app.ui.i18n.t("seller.phone_verified_short"))
        if (idVerified) VerifiedPill(com.soukmar.app.ui.i18n.t("seller.id_verified_short"))
    }
}

@Composable
private fun VerifiedPill(label: String) {
    Row(
        modifier = Modifier
            .background(SuccessLight, ShapePill)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = SuccessColor, modifier = Modifier.size(11.dp))
        Spacer(Modifier.width(2.dp))
        Text(label, color = SuccessColor, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** Error message of a form or screen: says what went wrong, with an icon so it is not carried by colour alone. */
@Composable
fun ErrorBanner(message: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().background(ErrorLight, ShapeControl).padding(Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = ErrorDark, modifier = Modifier.size(IconSize.small))
        Text(message, color = ErrorDark, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun SuccessBanner(message: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().background(SuccessLight, ShapeControl).padding(Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = SuccessDark, modifier = Modifier.size(IconSize.small))
        Text(message, color = SuccessDark, style = MaterialTheme.typography.bodyMedium)
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
            imageVector = if (isBusiness) Icons.Outlined.Business else Icons.Outlined.Person,
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
