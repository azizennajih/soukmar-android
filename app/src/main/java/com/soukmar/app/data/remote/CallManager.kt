package com.soukmar.app.data.remote

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dagger.hilt.android.qualifiers.ApplicationContext
import org.webrtc.AudioTrack
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.audio.JavaAudioDeviceModule
import javax.inject.Inject

enum class CallPhase { IDLE, OUTGOING, INCOMING, ACTIVE }

data class IncomingCallInfo(val conversationId: String, val fromUserId: String, val fromUserName: String)

/** Masked in-app voice calling over WebRTC (Tranche 15) — audio-only, free
 * public STUN only (no TURN relay, no paid telephony), signaled through the
 * *existing* chat Socket.IO connection ([ChatSocketManager]'s `call_*`
 * events). Mirrors the web's `ChatService` state machine 1:1
 * (idle/outgoing/incoming/active) — see `soukmar/src/app/services/
 * chat.service.ts`. Native WebRTC API surface via `io.getstream:stream-
 * webrtc-android` (Google's own `org.webrtc:google-webrtc` is no longer
 * reliably published to Maven Central; this is a maintained, free/Apache-2.0
 * fork of the same native libwebrtc that still publishes prebuilt AARs).
 *
 * Unscoped Hilt binding (no `@Singleton`) — [com.soukmar.app.ui.screens.chat.ChatViewModel]
 * gets a fresh instance per conversation screen, matching that PeerConnection's
 * lifecycle 1:1 rather than leaking a stale connection across conversations. */
class CallManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val socketManager: ChatSocketManager
) {
    companion object {
        // Free public STUN only — see class doc. No TURN relay, so a call
        // only connects when both peers are directly reachable; an accepted
        // tradeoff for a zero-cost calling feature (same as the web app).
        private val ICE_SERVERS = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer()
        )

        @Volatile private var factory: PeerConnectionFactory? = null

        private fun factory(context: Context): PeerConnectionFactory {
            return factory ?: synchronized(this) {
                factory ?: run {
                    PeerConnectionFactory.initialize(
                        PeerConnectionFactory.InitializationOptions.builder(context.applicationContext)
                            .createInitializationOptions()
                    )
                    val eglBase = EglBase.create()
                    PeerConnectionFactory.builder()
                        .setAudioDeviceModule(JavaAudioDeviceModule.builder(context.applicationContext).createAudioDeviceModule())
                        .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true))
                        .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBase.eglBaseContext))
                        .createPeerConnectionFactory()
                        .also { factory = it }
                }
            }
        }
    }

    var phase by mutableStateOf(CallPhase.IDLE)
        private set
    var incomingCall by mutableStateOf<IncomingCallInfo?>(null)
        private set
    var muted by mutableStateOf(false)
        private set
    var callError by mutableStateOf<String?>(null)
        private set

    private var peerConnection: PeerConnection? = null
    private var localAudioTrack: AudioTrack? = null
    private var activeConversationId: String? = null
    private var pendingOfferSdp: SessionDescription? = null

    /** Call from the conversation screen's socket-event collector, filtered
     * to `event.conversationId == this conversation` for the answer/ICE/end
     * cases (an offer can arrive for this conversation while idle, which is
     * the only case that sets up state from scratch). */
    fun handleEvent(event: ChatSocketEvent) {
        when (event) {
            is ChatSocketEvent.CallOffer -> {
                if (phase != CallPhase.IDLE) {
                    // Already on a call elsewhere — decline automatically
                    // instead of leaving the caller hanging, mirrors the web.
                    socketManager.emitCallEnd(event.conversationId)
                    return
                }
                pendingOfferSdp = SessionDescription(SessionDescription.Type.fromCanonicalForm(event.sdpType), event.sdp)
                activeConversationId = event.conversationId
                incomingCall = IncomingCallInfo(event.conversationId, event.fromUserId, event.fromUserName)
                phase = CallPhase.INCOMING
            }
            is ChatSocketEvent.CallAnswer -> {
                if (event.conversationId != activeConversationId) return
                peerConnection?.setRemoteDescription(
                    emptySdpObserver(),
                    SessionDescription(SessionDescription.Type.fromCanonicalForm(event.sdpType), event.sdp)
                )
                phase = CallPhase.ACTIVE
            }
            is ChatSocketEvent.CallIceCandidate -> {
                if (event.conversationId != activeConversationId) return
                try {
                    peerConnection?.addIceCandidate(IceCandidate(event.sdpMid, event.sdpMLineIndex, event.candidate))
                } catch (e: Exception) { /* candidate arrived after teardown — safe to ignore */ }
            }
            is ChatSocketEvent.CallEnded -> {
                if (event.conversationId != activeConversationId) return
                teardown()
            }
            else -> { /* not a call event */ }
        }
    }

    private fun createPeerConnection(conversationId: String): PeerConnection? {
        val rtcConfig = PeerConnection.RTCConfiguration(ICE_SERVERS)
        return factory(context).createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onIceCandidate(candidate: IceCandidate) {
                socketManager.emitCallIceCandidate(conversationId, candidate.sdp, candidate.sdpMid, candidate.sdpMLineIndex)
            }
            override fun onConnectionChange(newState: PeerConnection.PeerConnectionState) {
                if (newState == PeerConnection.PeerConnectionState.FAILED || newState == PeerConnection.PeerConnectionState.DISCONNECTED) {
                    teardown()
                }
            }
            override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>) {}
            override fun onAddStream(stream: MediaStream) {}
            override fun onRemoveStream(stream: MediaStream) {}
            override fun onDataChannel(dc: org.webrtc.DataChannel) {}
            override fun onRenegotiationNeeded() {}
            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) {}
            override fun onIceConnectionReceivingChange(receiving: Boolean) {}
            override fun onIceGatheringChange(state: PeerConnection.IceGatheringState) {}
            override fun onSignalingChange(state: PeerConnection.SignalingState) {}
            override fun onAddTrack(receiver: org.webrtc.RtpReceiver, streams: Array<out MediaStream>) {
                // Remote audio starts playing automatically through the
                // JavaAudioDeviceModule once the track is added — no manual
                // renderer needed for audio-only (unlike video).
            }
            override fun onTrack(transceiver: org.webrtc.RtpTransceiver) {}
        })
    }

    private fun addLocalAudio(pc: PeerConnection) {
        val audioSource = factory(context).createAudioSource(MediaConstraints())
        val track = factory(context).createAudioTrack("soukmar_audio0", audioSource)
        track.setEnabled(true)
        pc.addTrack(track, listOf("soukmar_stream0"))
        localAudioTrack = track
    }

    fun startCall(conversationId: String) {
        if (phase != CallPhase.IDLE) return
        callError = null
        val pc = createPeerConnection(conversationId) ?: run { callError = "call_failed"; return }
        peerConnection = pc
        activeConversationId = conversationId
        addLocalAudio(pc)
        pc.createOffer(object : SdpObserver by emptySdpObserver() {
            override fun onCreateSuccess(desc: SessionDescription) {
                pc.setLocalDescription(emptySdpObserver(), desc)
                socketManager.emitCallOffer(conversationId, desc.type.canonicalForm(), desc.description)
            }
            override fun onCreateFailure(error: String?) { callError = "call_failed" }
        }, MediaConstraints())
        phase = CallPhase.OUTGOING
    }

    fun acceptCall() {
        val info = incomingCall ?: return
        val offerSdp = pendingOfferSdp ?: return
        callError = null
        val pc = createPeerConnection(info.conversationId) ?: run { callError = "call_failed"; rejectCall(); return }
        peerConnection = pc
        addLocalAudio(pc)
        pc.setRemoteDescription(object : SdpObserver by emptySdpObserver() {
            override fun onSetSuccess() {
                pc.createAnswer(object : SdpObserver by emptySdpObserver() {
                    override fun onCreateSuccess(desc: SessionDescription) {
                        pc.setLocalDescription(emptySdpObserver(), desc)
                        socketManager.emitCallAnswer(info.conversationId, desc.type.canonicalForm(), desc.description)
                    }
                    override fun onCreateFailure(error: String?) { callError = "call_failed" }
                }, MediaConstraints())
            }
        }, offerSdp)
        incomingCall = null
        phase = CallPhase.ACTIVE
    }

    fun rejectCall() {
        incomingCall?.let { socketManager.emitCallEnd(it.conversationId) }
        teardown()
    }

    fun endCall() {
        activeConversationId?.let { socketManager.emitCallEnd(it) }
        teardown()
    }

    fun toggleMute() {
        val track = localAudioTrack ?: return
        val nowMuted = !muted
        track.setEnabled(!nowMuted)
        muted = nowMuted
    }

    private fun teardown() {
        localAudioTrack?.setEnabled(false)
        localAudioTrack = null
        peerConnection?.close()
        peerConnection = null
        pendingOfferSdp = null
        activeConversationId = null
        incomingCall = null
        muted = false
        phase = CallPhase.IDLE
    }

    private fun emptySdpObserver(): SdpObserver = object : SdpObserver {
        override fun onCreateSuccess(desc: SessionDescription) {}
        override fun onSetSuccess() {}
        override fun onCreateFailure(error: String?) {}
        override fun onSetFailure(error: String?) {}
    }
}
