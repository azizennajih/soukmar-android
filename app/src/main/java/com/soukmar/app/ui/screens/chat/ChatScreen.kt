@file:OptIn(ExperimentalMaterial3Api::class)

package com.soukmar.app.ui.screens.chat

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.soukmar.app.data.remote.CallPhase
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.soukmar.app.data.remote.dto.MessageDto
import com.soukmar.app.ui.components.VerifiedBadge
import com.soukmar.app.ui.i18n.t
import com.soukmar.app.ui.i18n.formatPricePartsT
import com.soukmar.app.ui.theme.BorderColor
import com.soukmar.app.ui.theme.ErrorColor
import com.soukmar.app.ui.theme.Gold
import com.soukmar.app.ui.theme.GoldLight
import com.soukmar.app.ui.theme.Primary
import com.soukmar.app.ui.theme.PrimaryLight
import com.soukmar.app.ui.theme.SuccessColor
import com.soukmar.app.ui.theme.TextMuted
import com.soukmar.app.ui.theme.TextPrimary
import com.soukmar.app.ui.theme.WhiteColor
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Composable
fun ChatScreen(
    conversationId: String,
    onBack: () -> Unit,
    onOpenListing: (String) -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    LaunchedEffect(conversationId) { viewModel.load(conversationId) }

    val context = LocalContext.current
    val micPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) viewModel.startCall()
    }
    fun startCallWithPermissionCheck() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            viewModel.startCall()
        } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
    val micPermissionLauncherForAccept = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) viewModel.acceptCall() else viewModel.rejectCall()
    }
    fun acceptCallWithPermissionCheck() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            viewModel.acceptCall()
        } else {
            micPermissionLauncherForAccept.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (viewModel.conversation != null) {
                        ChatHeaderContent(viewModel, onOpenListing)
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = t("chat.back")) } },
                actions = {
                    viewModel.conversation?.let { conv ->
                        if (!conv.blockedByMe && !conv.blockedByThem && viewModel.callManager.phase == CallPhase.IDLE) {
                            IconButton(onClick = { startCallWithPermissionCheck() }) {
                                Icon(Icons.Filled.Call, contentDescription = t("chat.call_start"), tint = Primary)
                            }
                        }
                        IconButton(onClick = { viewModel.requestBlockToggle() }, enabled = !viewModel.blockSubmitting) {
                            Icon(
                                if (conv.blockedByMe) Icons.Filled.RemoveCircleOutline else Icons.Filled.Block,
                                contentDescription = t(if (conv.blockedByMe) "block.unblock" else "block.block"),
                                tint = if (conv.blockedByMe) Primary else TextMuted
                            )
                        }
                        IconButton(onClick = { viewModel.reportOpen = true }) {
                            Icon(Icons.Filled.Flag, contentDescription = "Signaler", tint = TextMuted)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                viewModel.callManager.callError?.let {
                    com.soukmar.app.ui.components.ErrorBanner(t("chat.call_failed"), modifier = Modifier.padding(12.dp))
                }
                if (viewModel.callManager.phase == CallPhase.OUTGOING || viewModel.callManager.phase == CallPhase.ACTIVE) {
                    ActiveCallBar(viewModel)
                }
                Box(modifier = Modifier.weight(1f)) {
                    when {
                        viewModel.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
                        viewModel.loadError || viewModel.conversation == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Conversation introuvable.", color = TextMuted)
                        }
                        else -> ChatContent(viewModel)
                    }
                }
            }
        }
    }

    if (viewModel.callManager.phase == CallPhase.INCOMING) {
        IncomingCallDialog(
            callerName = viewModel.callManager.incomingCall?.fromUserName ?: "",
            onAccept = { acceptCallWithPermissionCheck() },
            onReject = { viewModel.rejectCall() }
        )
    }

    if (viewModel.confirmCancelReservation) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissCancelReservation() },
            title = { Text("${t("chat.cancel_reservation")} ?") },
            text = { Text("L'annonce redevient active.") },
            confirmButton = { TextButton(onClick = { viewModel.confirmCancelReservation() }) { Text("Confirmer", color = ErrorColor) } },
            dismissButton = { TextButton(onClick = { viewModel.dismissCancelReservation() }) { Text(t("chat.cancel")) } }
        )
    }
    if (viewModel.confirmCancelOfferId != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissCancelOffer() },
            title = { Text("Annuler votre offre ?") },
            confirmButton = { TextButton(onClick = { viewModel.confirmCancelOffer() }) { Text("Confirmer", color = ErrorColor) } },
            dismissButton = { TextButton(onClick = { viewModel.dismissCancelOffer() }) { Text(t("chat.cancel")) } }
        )
    }
    if (viewModel.reportOpen) {
        ReportDialog(viewModel)
    }
    if (viewModel.confirmBlock) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissBlockConfirm() },
            title = { Text(t("block.block")) },
            text = { Text(t("block.confirm")) },
            confirmButton = { TextButton(onClick = { viewModel.confirmBlockToggle() }) { Text(t("block.block"), color = ErrorColor) } },
            dismissButton = { TextButton(onClick = { viewModel.dismissBlockConfirm() }) { Text(t("chat.cancel")) } }
        )
    }
}

@Composable
private fun ChatHeaderContent(viewModel: ChatViewModel, onOpenListing: (String) -> Unit) {
    val conv = viewModel.conversation ?: return
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(32.dp).background(Primary, CircleShape), contentAlignment = Alignment.Center) {
                Text(viewModel.partnerName().take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(Modifier.width(8.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(viewModel.partnerName(), fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    viewModel.partnerUser()?.let { partner ->
                        if (partner.reviewCount > 0) {
                            Spacer(Modifier.width(6.dp))
                            CompactStars(partner.avgRating)
                        }
                    }
                }
                viewModel.partnerUser()?.let { partner ->
                    VerifiedBadge(partner.emailVerified, partner.phoneVerified, partner.idVerified, modifier = Modifier.padding(vertical = 1.dp))
                }
                if (viewModel.partnerTyping) {
                    Text(t("chat.typing"), fontSize = 11.sp, color = Primary)
                } else {
                    Text(
                        conv.listing.title,
                        fontSize = 11.sp,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable { onOpenListing(conv.listingId) }
                    )
                }
            }
        }
    }
}

/** Outgoing/active call bar — mirrors the web's call bar markup in
 * `chat.component.html` (mute toggle + end-call button, status text swaps
 * between "ringing…" and "in call"). */
@Composable
private fun ActiveCallBar(viewModel: ChatViewModel) {
    val active = viewModel.callManager.phase == CallPhase.ACTIVE
    Row(
        modifier = Modifier.fillMaxWidth().background(if (active) SuccessColor else Gold).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            if (active) t("chat.call_active") else t("chat.call_ringing"),
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = { viewModel.toggleMute() }) {
            Icon(
                if (viewModel.callManager.muted) Icons.Filled.MicOff else Icons.Filled.Mic,
                contentDescription = t(if (viewModel.callManager.muted) "chat.call_unmute" else "chat.call_mute"),
                tint = Color.White
            )
        }
        IconButton(onClick = { viewModel.endCall() }) {
            Icon(Icons.Filled.CallEnd, contentDescription = t("chat.call_end"), tint = Color.White)
        }
    }
}

/** Incoming-call full-screen overlay — mirrors the web's incoming-call
 * card (caller name, accept/reject) in `chat.component.html`. */
@Composable
private fun IncomingCallDialog(callerName: String, onAccept: () -> Unit, onReject: () -> Unit) {
    Dialog(onDismissRequest = onReject, properties = DialogProperties(dismissOnClickOutside = false)) {
        Column(
            modifier = Modifier.fillMaxWidth().background(WhiteColor, RoundedCornerShape(20.dp)).padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.size(72.dp).background(Primary, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(t("chat.call_incoming"), color = TextMuted, fontSize = 13.sp)
            Spacer(Modifier.height(4.dp))
            Text(callerName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = onReject,
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorColor),
                    modifier = Modifier.size(56.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = CircleShape
                ) { Icon(Icons.Filled.CallEnd, contentDescription = t("chat.call_reject"), tint = Color.White) }
                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessColor),
                    modifier = Modifier.size(56.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = CircleShape
                ) { Icon(Icons.Filled.Call, contentDescription = t("chat.call_accept"), tint = Color.White) }
            }
        }
    }
}

@Composable
private fun CompactStars(avgRating: Double?) {
    val rating = avgRating?.let { Math.round(it).toInt() } ?: 0
    Row {
        (1..5).forEach { s ->
            Text("★", fontSize = 10.sp, color = if (s <= rating) Gold else BorderColor)
        }
    }
}

@Composable
private fun ChatContent(viewModel: ChatViewModel) {
    val listState = rememberLazyListState()

    LaunchedEffect(viewModel.messages.size) {
        if (viewModel.messages.isNotEmpty()) listState.animateScrollToItem(viewModel.messages.size - 1)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (viewModel.listingStatus == "RESERVED") {
            Row(
                modifier = Modifier.fillMaxWidth().background(GoldLight).padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Lock, contentDescription = null, tint = Gold, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text("${t("chat.reserved_msg")} ${t("chat.reserved_word")}", fontSize = 13.sp, color = Gold, modifier = Modifier.weight(1f))
                TextButton(onClick = { viewModel.requestCancelReservation() }) { Text(t("chat.cancel"), fontSize = 12.sp) }
            }
        }

        if (viewModel.reportSubmitted) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SuccessColor, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text("Signalement envoyé, merci.", color = SuccessColor, fontSize = 13.sp)
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(viewModel.messages, key = { it.id }) { msg ->
                when {
                    viewModel.isSystem(msg) -> SystemMessageRow(msg)
                    viewModel.isOffer(msg) -> OfferBubble(
                        msg = msg,
                        currency = viewModel.conversation?.listing?.currency ?: "MAD",
                        mine = viewModel.isMine(msg),
                        canRespond = viewModel.canRespond(msg),
                        canCancel = viewModel.canCancel(msg),
                        onAccept = { viewModel.respondOffer(msg, "ACCEPTED") },
                        onReject = { viewModel.respondOffer(msg, "REJECTED") },
                        onCancel = { viewModel.requestCancelOffer(msg) }
                    )
                    else -> TextBubble(msg, viewModel.isMine(msg))
                }
            }
        }

        if (viewModel.messagingBlocked()) {
            Row(
                modifier = Modifier.fillMaxWidth().background(BorderColor.copy(alpha = 0.4f)).padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Block, contentDescription = null, tint = TextMuted, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text(t("chat.blocked_banner"), color = TextMuted, fontSize = 13.sp, textAlign = TextAlign.Center)
            }
        } else {
            if (viewModel.showOfferInput) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(PrimaryLight).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.LocalOffer, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = viewModel.offerAmount,
                        onValueChange = { if (it.all { c -> c.isDigit() }) viewModel.offerAmount = it },
                        placeholder = { Text("Montant en ${viewModel.conversation?.listing?.currency ?: "MAD"}") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, cursorColor = Primary)
                    )
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = { viewModel.sendOffer() }, enabled = viewModel.offerAmount.isNotBlank()) { Text(t("chat.send_offer")) }
                    TextButton(onClick = { viewModel.showOfferInput = false }) { Text(t("chat.cancel")) }
                }
            }

            val quickReplies = listOf(t("chat.quick_available"), t("chat.quick_last_price"), t("chat.quick_still_interested"), t("chat.quick_thanks"))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickReplies.forEach { reply ->
                    OutlinedButton(onClick = { viewModel.useQuickReply(reply) }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                        Text(reply, fontSize = 12.sp)
                    }
                }
            }

            HorizontalDivider(color = BorderColor)
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                IconButton(onClick = { viewModel.showOfferInput = !viewModel.showOfferInput }) {
                    Icon(Icons.Filled.LocalOffer, contentDescription = t("chat.send_offer"), tint = Gold)
                }
                OutlinedTextField(
                    value = viewModel.messageText,
                    onValueChange = { viewModel.messageText = it; viewModel.onTyping() },
                    placeholder = { Text(t("chat.placeholder")) },
                    modifier = Modifier.weight(1f),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { viewModel.sendMessage() }),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, cursorColor = Primary)
                )
                IconButton(onClick = { viewModel.sendMessage() }, enabled = viewModel.messageText.isNotBlank()) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Envoyer", tint = if (viewModel.messageText.isNotBlank()) Primary else TextMuted)
                }
            }
        }
    }
}

@Composable
private fun SystemMessageRow(msg: MessageDto) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            msg.content,
            fontSize = 11.sp,
            color = TextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.background(BorderColor.copy(alpha = 0.4f), RoundedCornerShape(999.dp)).padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun MsgTimeLabel(iso: String) {
    val (date, time) = formatMsgTimeParts(iso)
    Column(horizontalAlignment = Alignment.Start) {
        Text(date, fontSize = 10.sp, color = TextMuted, lineHeight = 12.sp)
        Text(time, fontSize = 10.sp, color = TextMuted, lineHeight = 12.sp)
    }
}

@Composable
private fun TextBubble(msg: MessageDto, mine: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (mine) Alignment.End else Alignment.Start
    ) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            MsgTimeLabel(msg.createdAt)
            Box(
                modifier = Modifier
                    .background(if (mine) Primary else WhiteColor, RoundedCornerShape(14.dp))
                    .border(if (mine) 0.dp else 1.dp, BorderColor, RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .fillMaxWidth(0.72f)
            ) {
                Text(msg.content, color = if (mine) Color.White else TextPrimary, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun OfferBubble(
    msg: MessageDto,
    currency: String,
    mine: Boolean,
    canRespond: Boolean,
    canCancel: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onCancel: () -> Unit
) {
    val amountText = msg.offerAmount?.let { formatPricePartsT(it, currency).first } ?: "—"
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = if (mine) Alignment.End else Alignment.Start) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        MsgTimeLabel(msg.createdAt)
        Column(
            modifier = Modifier
                .background(GoldLight, RoundedCornerShape(14.dp))
                .border(1.dp, Gold, RoundedCornerShape(14.dp))
                .padding(12.dp)
                .fillMaxWidth(0.68f)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocalOffer, contentDescription = null, tint = Gold, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(4.dp))
                Text(t("chat.offer_price"), fontSize = 11.sp, color = Gold, fontWeight = FontWeight.SemiBold)
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text(amountText, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(Modifier.width(4.dp))
                Text(t("common.mad"), fontSize = 11.sp, color = TextMuted)
            }
            when (msg.offerStatus) {
                "PENDING" -> StatusRow(Icons.Filled.HourglassEmpty, t("chat.pending"), TextMuted)
                "ACCEPTED" -> StatusRow(Icons.Filled.CheckCircle, t("chat.accepted"), SuccessColor)
                "REJECTED" -> if (mine) StatusRow(Icons.Filled.Block, t("chat.cancelled"), ErrorColor) else StatusRow(Icons.Filled.Cancel, t("chat.rejected"), ErrorColor)
            }
            if (canRespond) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onAccept, colors = ButtonDefaults.buttonColors(containerColor = SuccessColor), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(t("chat.accept"), fontSize = 12.sp)
                    }
                    Button(onClick = onReject, colors = ButtonDefaults.buttonColors(containerColor = ErrorColor), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(t("chat.reject"), fontSize = 12.sp)
                    }
                }
            }
            if (canCancel) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onCancel, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                    Icon(Icons.Filled.Block, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(t("chat.cancel_offer"), fontSize = 12.sp)
                }
            }
        }
        }
    }
}

@Composable
private fun StatusRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 11.sp, color = color)
    }
}

@Composable
private fun ReportDialog(viewModel: ChatViewModel) {
    AlertDialog(
        onDismissRequest = { viewModel.cancelReport() },
        title = { Text("Pourquoi signalez-vous cette personne ?") },
        text = {
            Column {
                OutlinedTextField(
                    value = viewModel.reportReason,
                    onValueChange = { viewModel.reportReason = it },
                    placeholder = { Text("Décrivez le problème…") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, cursorColor = Primary)
                )
                viewModel.reportError?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(it, color = ErrorColor, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.submitReport() }, enabled = !viewModel.reportSubmitting) {
                Text(if (viewModel.reportSubmitting) "Envoi…" else t("report.submit"))
            }
        },
        dismissButton = { TextButton(onClick = { viewModel.cancelReport() }) { Text(t("chat.cancel")) } }
    )
}

private val msgDateFormatter = DateTimeFormatter.ofPattern("dd.MM.").withZone(ZoneId.systemDefault())
private val msgHourFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

private fun formatMsgTimeParts(iso: String): Pair<String, String> {
    return try {
        val instant = Instant.parse(iso)
        msgDateFormatter.format(instant) to msgHourFormatter.format(instant)
    } catch (e: DateTimeParseException) {
        "" to ""
    }
}
