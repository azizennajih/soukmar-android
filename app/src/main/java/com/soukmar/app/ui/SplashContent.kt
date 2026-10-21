package com.soukmar.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.soukmar.app.R

/** Start screen: brand red over the whole screen, white SouqMar24 wordmark in the middle (same red as the system start screen,
 * so the hand-over is seamless). Shown until the start destination (login or home) is known. */
@Composable
fun SplashContent() {
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFFD93D4A)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.splash_logo),
            contentDescription = "SouqMar24",
            modifier = Modifier.width(224.dp)
        )
    }
}
