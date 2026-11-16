package com.soukmar.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soukmar.app.ui.components.SoukMarMark
import com.soukmar.app.ui.theme.Primary
import com.soukmar.app.ui.theme.TextPrimary
import com.soukmar.app.ui.theme.WhiteColor

/** Start screen: white ground, the red logo tile exactly in the middle (same size and place as the system start screen,
 * res/drawable/splash_mark.xml, so the hand-over is seamless), the wordmark fades in below it. No progress indicator.
 * Shown until the start destination (login or home) is known. */
@Composable
fun SplashContent() {
    val wordmark = remember { Animatable(0f) }
    LaunchedEffect(Unit) { wordmark.animateTo(1f, tween(durationMillis = 350, delayMillis = 100)) }
    Box(
        modifier = Modifier.fillMaxSize().background(WhiteColor).clearAndSetSemantics { contentDescription = "SouqMar24" },
        contentAlignment = Alignment.Center
    ) {
        SoukMarMark(size = 96.dp)
        // The wordmark is a brand: always left to right.
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(modifier = Modifier.offset(y = (84 - 8 * (1f - wordmark.value)).dp).alpha(wordmark.value)) {
                Text("SouqMar", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 28.sp, letterSpacing = (-0.4).sp)
                Text("24", color = Primary, fontWeight = FontWeight.Black, fontSize = 28.sp, letterSpacing = (-0.4).sp)
            }
        }
    }
}
