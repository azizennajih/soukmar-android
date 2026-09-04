package com.soukmar.app.ui.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.soukmar.app.data.local.TokenManager
import com.soukmar.app.data.remote.ApiService
import com.soukmar.app.data.remote.dto.UserDto
import com.soukmar.app.data.repository.NotificationRepository
import com.soukmar.app.data.repository.PushRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val api: ApiService,
    private val tokenManager: TokenManager,
    private val notificationRepository: NotificationRepository,
    private val pushRepository: PushRepository
) : ViewModel() {

    var user by mutableStateOf<UserDto?>(null)
    var loading by mutableStateOf(true)
    var unreadNotifications by mutableStateOf(0)
        private set

    init {
        viewModelScope.launch {
            loading = true
            try {
                val res = api.me()
                if (res.isSuccessful) user = res.body()
            } catch (_: Exception) { }
            loading = false
        }
        registerFcmToken()
        startUnreadPolling()
    }

    private fun registerFcmToken() {
        viewModelScope.launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                pushRepository.registerToken(token)
            } catch (_: Exception) {
                // Firebase isn't configured with real project credentials yet
                // (see google-services.json) — fail silently, in-app
                // notifications still work without device push.
            }
        }
    }

    /** Mirrors the web navbar's 30-second unread-count poll — the backend
     * has no live-update mechanism for the badge (see soukmar-android's
     * CLAUDE.md phase 11 notes). */
    private fun startUnreadPolling() {
        viewModelScope.launch {
            while (true) {
                unreadNotifications = notificationRepository.getUnreadCount()
                delay(30_000)
            }
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            tokenManager.clear()
            onDone()
        }
    }
}
