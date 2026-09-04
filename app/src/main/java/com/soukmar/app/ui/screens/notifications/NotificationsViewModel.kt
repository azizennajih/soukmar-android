package com.soukmar.app.ui.screens.notifications

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soukmar.app.data.remote.dto.NotificationDto
import com.soukmar.app.data.repository.ApiResult
import com.soukmar.app.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    var notifications by mutableStateOf<List<NotificationDto>>(emptyList())
        private set
    var loading by mutableStateOf(true)
        private set

    val hasUnread: Boolean get() = notifications.any { !it.isRead }

    fun load() {
        viewModelScope.launch {
            loading = true
            when (val result = notificationRepository.getAll()) {
                is ApiResult.Success -> notifications = result.data
                is ApiResult.Error -> { /* keep whatever list was already shown */ }
            }
            loading = false
        }
    }

    /** Optimistically marks [notification] read locally, then confirms with
     * the backend — mirrors the web's open() handler. */
    fun markRead(notification: NotificationDto) {
        if (notification.isRead) return
        notifications = notifications.map { if (it.id == notification.id) it.copy(isRead = true) else it }
        viewModelScope.launch { notificationRepository.markRead(notification.id) }
    }

    fun markAllRead() {
        if (!hasUnread) return
        notifications = notifications.map { it.copy(isRead = true) }
        viewModelScope.launch { notificationRepository.markAllRead() }
    }
}
