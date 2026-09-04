package com.soukmar.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.soukmar.app.data.local.TokenManager
import com.soukmar.app.ui.navigation.Routes
import com.soukmar.app.ui.navigation.SoukMarNavGraph
import com.soukmar.app.ui.theme.SoukMarTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SoukMarTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var startDestination by remember { mutableStateOf<String?>(null) }

                    LaunchedEffect(Unit) {
                        startDestination = when {
                            !tokenManager.isLoggedIn() -> Routes.LOGIN
                            intent?.getBooleanExtra("open_notifications", false) == true -> Routes.NOTIFICATIONS
                            else -> Routes.HOME
                        }
                    }

                    val dest = startDestination
                    if (dest == null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        SoukMarNavGraph(startDestination = dest)
                    }
                }
            }
        }
    }
}
