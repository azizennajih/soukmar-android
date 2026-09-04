package com.soukmar.app.ui.screens.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soukmar.app.data.local.TokenManager
import com.soukmar.app.data.remote.ChatSocketManager
import com.soukmar.app.data.remote.dto.ConversationDto
import com.soukmar.app.data.repository.ApiResult
import com.soukmar.app.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val socketManager: ChatSocketManager,
    private val tokenManager: TokenManager
) : ViewModel() {

    var conversations by mutableStateOf<List<ConversationDto>>(emptyList())
        private set
    var loading by mutableStateOf(true)
        private set
    var currentUserId by mutableStateOf<String?>(null)
        private set

    fun load() {
        viewModelScope.launch {
            loading = true
            currentUserId = tokenManager.currentUserId()
            tokenManager.getToken()?.let { socketManager.connect(it) }
            when (val result = chatRepository.getConversations()) {
                is ApiResult.Success -> conversations = result.data
                is ApiResult.Error -> { /* empty list is a fine fallback for the list screen */ }
            }
            loading = false
        }
    }

    // Mirrors the web app's ChatComponent: the socket connection lives for as
    // long as the chat section is open, torn down when leaving it entirely.
    override fun onCleared() {
        socketManager.disconnect()
    }
}
