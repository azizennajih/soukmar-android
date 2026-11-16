package com.soukmar.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

// The whole Material palette is mapped onto the brand: white surfaces, red only where Material uses "primary"
// (buttons, selection, focus), neutral greys for containers. surfaceTint is white so that raised surfaces
// (menus, dialogs, bars) stay white instead of turning pink.
private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = WhiteColor,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = PrimaryDark,
    inversePrimary = PrimaryLight,
    secondary = Secondary,
    onSecondary = WhiteColor,
    secondaryContainer = PrimaryLight,
    onSecondaryContainer = PrimaryDark,
    tertiary = Gold,
    onTertiary = WhiteColor,
    tertiaryContainer = GoldLight,
    onTertiaryContainer = Gold,
    background = BgColor,
    onBackground = TextPrimary,
    surface = WhiteColor,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceMuted,
    onSurfaceVariant = TextMuted,
    surfaceTint = WhiteColor,
    inverseSurface = Secondary,
    inverseOnSurface = WhiteColor,
    error = ErrorColor,
    onError = WhiteColor,
    errorContainer = ErrorLight,
    onErrorContainer = ErrorDark,
    outline = BorderColor,
    outlineVariant = BorderColor,
    surfaceBright = WhiteColor,
    surfaceDim = SurfaceMuted,
    surfaceContainerLowest = WhiteColor,
    surfaceContainerLow = WhiteColor,
    surfaceContainer = WhiteColor,
    surfaceContainerHigh = SurfaceMuted,
    surfaceContainerHighest = SurfaceMuted,
)

private val SoukMarShapes = Shapes(
    extraSmall = ShapeBadge,
    small = ShapeControl,
    medium = ShapeCard,
    large = ShapeCard,
    extraLarge = RoundedCornerShape(24.dp),
)


/** Light only: the brand is defined on a white ground, and the screens use the fixed brand colours.
 * A dark variant needs its own palette and is not part of this design yet. */
@Composable
fun SoukMarTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = SoukMarTypography,
        shapes = SoukMarShapes,
        content = content
    )
}
