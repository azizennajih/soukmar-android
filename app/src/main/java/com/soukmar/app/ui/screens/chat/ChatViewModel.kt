package com.soukmar.app.ui.screens.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soukmar.app.data.local.TokenManager
import com.soukmar.app.data.remote.ChatSocketEvent
import com.soukmar.app.data.remote.ChatSocketManager
import com.soukmar.app.data.remote.dto.ConversationDto
import com.soukmar.app.data.remote.dto.MessageDto
import com.soukmar.app.data.remote.dto.partnerId
import com.soukmar.app.data.remote.dto.partnerName
import com.soukmar.app.data.repository.ApiResult
import com.soukmar.app.data.repository.ChatRepository
import com.soukmar.app.data.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val reportRepository: ReportRepository,
    private val socketManager: ChatSocketManager,
    private val tokenManager: TokenManager
) : ViewModel() {

    private var conversationId: String? = null

    var conversation by mutableStateOf<ConversationDto?>(null)
        private set
    var messages by mutableStateOf<List<MessageDto>>(emptyList())
        private set
    var loading by mutableStateOf(true)
        private set
    var loadError by mutableStateOf(false)
        private set
    var currentUserId by mutableStateOf<String?>(null)
        private set
    var listingStatus by mutableStateOf("")
        private set
    var partnerTyping by mutableStateOf(false)
        private set

    var messageText by mutableStateOf("")
    var offerAmount by mutableStateOf("")
    var showOfferInput by mutableStateOf(false)

    var reportOpen by mutableStateOf(false)
    var reportReason by mutableStateOf("")
    var reportSubmitting by mutableStateOf(false)
        private set
    var reportSubmitted by mutableStateOf(false)
        private set
    var reportError by mutableStateOf<String?>(null)
        private set

    var confirmCancelReservation by mutableStateOf(false)
    var confirmCancelOfferId by mutableStateOf<String?>(null)

    private var typingJob: Job? = null

    fun load(id: String) {
        if (conversationId == id && conversation != null) return
        conversationId = id
        viewModelScope.launch {
            loading = true
            loadError = false
            currentUserId = tokenManager.currentUserId()
            tokenManager.getToken()?.let { socketManager.connect(it) }

            when (val result = chatRepository.getConversations()) {
                is ApiResult.Success -> conversation = result.data.find { it.id == id }
                is ApiResult.Error -> { /* fall through — messages fetch below still tells us if the conversation is real */ }
            }
            if (conversation == null) {
                loadError = true
                loading = false
                return@launch
            }
            listingStatus = conversation?.listing?.status ?: ""

            when (val result = chatRepository.getMessages(id)) {
                is ApiResult.Success -> messages = result.data
                is ApiResult.Error -> { /* start with an empty thread rather than blocking the screen */ }
            }

            socketManager.joinConversation(id)
            observeSocketEvents(id)
            loading = false
        }
    }

    private fun observeSocketEvents(id: String) {
        viewModelScope.launch {
            socketManager.events.collect { event ->
                when (event) {
                    is ChatSocketEvent.NewMessage -> if (event.message.conversationId == id) {
                        messages = messages + event.message
                    }
                    is ChatSocketEvent.OfferUpdated -> {
                        messages = messages.map { if (it.id == event.message.id) event.message else it }
                    }
                    is ChatSocketEvent.UserTyping -> partnerTyping = event.isTyping
                    is ChatSocketEvent.ListingStatusChanged -> if (event.listingId == conversation?.listingId) {
                        listingStatus = event.status
                    }
                }
            }
        }
    }

    fun partnerId(): String? = conversation?.partnerId(currentUserId)
    fun partnerName(): String = conversation?.partnerName(currentUserId) ?: ""
    fun isMine(msg: MessageDto): Boolean = msg.senderId == currentUserId
    fun isOffer(msg: MessageDto): Boolean = msg.type == "OFFER"
    fun isSystem(msg: MessageDto): Boolean = msg.type == "SYSTEM"
    fun canRespond(msg: MessageDto): Boolean = msg.type == "OFFER" && msg.offerStatus == "PENDING" && !isMine(msg)
    fun canCancel(msg: MessageDto): Boolean = msg.type == "OFFER" && msg.offerStatus == "PENDING" && isMine(msg)

    fun sendMessage() {
        val text = messageText.trim()
        val conv = conversation ?: return
        val partner = partnerId() ?: return
        if (text.isEmpty()) return
        socketManager.sendMessage(conv.id, partner, conv.listingId, text)
        messageText = ""
        socketManager.emitTyping(conv.id, false)
        typingJob?.cancel()
    }

    fun onTyping() {
        val conv = conversation ?: return
        socketManager.emitTyping(conv.id, true)
        typingJob?.cancel()
        typingJob = viewModelScope.launch {
            delay(2000)
            socketManager.emitTyping(conv.id, false)
        }
    }

    fun sendOffer() {
        val amount = offerAmount.toDoubleOrNull() ?: return
        val conv = conversation ?: return
        val partner = partnerId() ?: return
        if (amount <= 0) return
        socketManager.sendOffer(conv.id, partner, conv.listingId, amount)
        offerAmount = ""
        showOfferInput = false
    }

    fun respondOffer(msg: MessageDto, status: String) {
        val conv = conversation ?: return
        socketManager.respondOffer(msg.id, conv.id, status)
    }

    fun requestCancelOffer(msg: MessageDto) { confirmCancelOfferId = msg.id }
    fun dismissCancelOffer() { confirmCancelOfferId = null }
    fun confirmCancelOffer() {
        val conv = conversation ?: return
        val msgId = confirmCancelOfferId ?: return
        socketManager.cancelOffer(msgId, conv.id, conv.listingId)
        confirmCancelOfferId = null
    }

    fun requestCancelReservation() { confirmCancelReservation = true }
    fun dismissCancelReservation() { confirmCancelReservation = false }
    fun confirmCancelReservation() {
        val conv = conversation ?: return
        socketManager.cancelReservation(conv.id, conv.listingId)
        listingStatus = "ACTIVE"
        confirmCancelReservation = false
    }

    fun useQuickReply(text: String) { messageText = text }

    fun submitReport() {
        val conv = conversation ?: return
        val partner = partnerId() ?: return
        if (reportReason.trim().length < 10) {
            reportError = "Merci de décrire la raison en au moins 10 caractères."
            return
        }
        reportSubmitting = true
        reportError = null
        viewModelScope.launch {
            when (val result = reportRepository.submit(partner, conv.listingId, reportReason.trim())) {
                is ApiResult.Success -> {
                    reportSubmitting = false
                    reportSubmitted = true
                    reportOpen = false
                }
                is ApiResult.Error -> {
                    reportSubmitting = false
                    reportError = result.message
                }
            }
        }
    }

    fun cancelReport() {
        reportOpen = false
        reportError = null
    }

    override fun onCleared() {
        typingJob?.cancel()
    }
}
