package com.soukmar.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.soukmar.app.data.i18n.I18nRepository
import com.soukmar.app.data.local.TokenManager
import com.soukmar.app.ui.i18n.LocalI18n
import com.soukmar.app.ui.navigation.Routes
import com.soukmar.app.ui.SplashContent
import com.soukmar.app.ui.navigation.SoukMarNavGraph
import com.soukmar.app.ui.theme.SoukMarTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var tokenManager: TokenManager
    private companion object {
        const val SPLASH_MIN_MS = 700L
    }

    @Inject lateinit var i18nRepository: I18nRepository
    @Inject lateinit var countryRepository: com.soukmar.app.data.country.CountryRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        // The system start screen is plain brand red (Theme.SoukMar.Starting); the logo is drawn right on it by SplashContent below,
        // because Android 12+ limits a system start-screen icon to a small circle.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val layoutDirection = if (i18nRepository.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
            CompositionLocalProvider(
                LocalI18n provides i18nRepository,
                com.soukmar.app.ui.i18n.LocalCountry provides countryRepository.country,
                LocalLayoutDirection provides layoutDirection
            ) {
            SoukMarTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var startDestination by remember { mutableStateOf<String?>(null) }

                    LaunchedEffect(Unit) {
                        val destination = when {
                            !tokenManager.isLoggedIn() -> Routes.LOGIN
                            intent?.getBooleanExtra("open_notifications", false) == true -> Routes.NOTIFICATIONS
                            else -> Routes.HOME
                        }
                        // Brand moment: on a fresh start the logo stays visible for a short while (not when the system only recreates the activity).
                        if (savedInstanceState == null) delay(SPLASH_MIN_MS)
                        startDestination = destination
                    }

                    val dest = startDestination
                    if (dest == null) {
                        SplashContent()
                    } else {
                        SoukMarNavGraph(startDestination = dest)
                    }
                }
            }
            }
        }
    }
}
