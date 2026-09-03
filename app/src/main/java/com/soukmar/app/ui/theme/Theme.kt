package com.soukmar.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = WhiteColor,
    secondary = Secondary,
    onSecondary = WhiteColor,
    background = BgColor,
    onBackground = TextPrimary,
    surface = WhiteColor,
    onSurface = TextPrimary,
    error = ErrorColor,
    outline = BorderColor,
)

private val DarkColors = darkColorScheme(
    primary = Primary,
    onPrimary = WhiteColor,
    secondary = Secondary,
    onSecondary = WhiteColor,
    background = Color(0xFF111827),
    onBackground = WhiteColor,
    surface = Color(0xFF1F2937),
    onSurface = WhiteColor,
    error = ErrorColor,
)

@Composable
fun SoukMarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = SoukMarTypography,
        content = content
    )
}
