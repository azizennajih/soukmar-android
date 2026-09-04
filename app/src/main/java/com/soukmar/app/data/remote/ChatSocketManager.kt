package com.soukmar.app.data.remote

import com.soukmar.app.BuildConfig
import com.soukmar.app.data.remote.dto.MessageDto
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.json.Json
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

sealed class ChatSocketEvent {
    data class NewMessage(val message: MessageDto) : ChatSocketEvent()
    data class OfferUpdated(val message: MessageDto) : ChatSocketEvent()
    data class UserTyping(val isTyping: Boolean) : ChatSocketEvent()
    data class ListingStatusChanged(val listingId: String, val status: String) : ChatSocketEvent()
}

/** Thin wrapper around socket.io-client mirroring the web app's ChatService —
 * same event names/payloads as soukmar-backend/src/socket.ts, since the
 * backend only speaks Socket.IO for chat (no REST endpoints to send a
 * message or respond to an offer). Callbacks fire on socket.io's own
 * background thread; events are republished on a SharedFlow so ViewModels
 * can safely collect them from viewModelScope. */
@Singleton
class ChatSocketManager @Inject constructor(private val json: Json) {

    private var socket: Socket? = null

    private val _events = MutableSharedFlow<ChatSocketEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<ChatSocketEvent> = _events.asSharedFlow()

    fun connect(token: String) {
        if (socket?.connected() == true) return
        val opts = IO.Options()
        opts.auth = mapOf("token" to token)
        opts.reconnection = true
        val s = IO.socket(java.net.URI.create(BuildConfig.SOCKET_URL), opts)
        s.on("new_message") { args -> decodeMessage(args)?.let { _events.tryEmit(ChatSocketEvent.NewMessage(it)) } }
        s.on("offer_updated") { args -> decodeMessage(args)?.let { _events.tryEmit(ChatSocketEvent.OfferUpdated(it)) } }
        s.on("user_typing") { args ->
            val obj = args.getOrNull(0) as? JSONObject ?: return@on
            _events.tryEmit(ChatSocketEvent.UserTyping(obj.optBoolean("isTyping", false)))
        }
        s.on("listing_status_changed") { args ->
            val obj = args.getOrNull(0) as? JSONObject ?: return@on
            val listingId = obj.optString("listingId").takeIf { it.isNotEmpty() } ?: return@on
            _events.tryEmit(ChatSocketEvent.ListingStatusChanged(listingId, obj.optString("status", "ACTIVE")))
        }
        s.connect()
        socket = s
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
    }

    fun joinConversation(conversationId: String) {
        socket?.emit("join_conversation", conversationId)
    }

    fun sendMessage(conversationId: String, receiverId: String, listingId: String, content: String) {
        socket?.emit("send_message", JSONObject().apply {
            put("conversationId", conversationId)
            put("receiverId", receiverId)
            put("listingId", listingId)
            put("content", content)
        })
    }

    fun sendOffer(conversationId: String, receiverId: String, listingId: String, amount: Double) {
        socket?.emit("send_offer", JSONObject().apply {
            put("conversationId", conversationId)
            put("receiverId", receiverId)
            put("listingId", listingId)
            put("amount", amount)
        })
    }

    fun respondOffer(messageId: String, conversationId: String, status: String) {
        socket?.emit("respond_offer", JSONObject().apply {
            put("messageId", messageId)
            put("conversationId", conversationId)
            put("status", status)
        })
    }

    fun cancelOffer(messageId: String, conversationId: String, listingId: String) {
        socket?.emit("cancel_offer", JSONObject().apply {
            put("messageId", messageId)
            put("conversationId", conversationId)
            put("listingId", listingId)
        })
    }

    fun cancelReservation(conversationId: String, listingId: String) {
        socket?.emit("cancel_reservation", JSONObject().apply {
            put("conversationId", conversationId)
            put("listingId", listingId)
        })
    }

    fun emitTyping(conversationId: String, isTyping: Boolean) {
        socket?.emit("typing", JSONObject().apply {
            put("conversationId", conversationId)
            put("isTyping", isTyping)
        })
    }

    private fun decodeMessage(args: Array<Any>): MessageDto? {
        val obj = args.getOrNull(0) as? JSONObject ?: return null
        return try {
            json.decodeFromString(MessageDto.serializer(), obj.toString())
        } catch (e: Exception) {
            null
        }
    }
}
