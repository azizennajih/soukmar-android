@file:OptIn(ExperimentalMaterial3Api::class)

package com.soukmar.app.ui.screens.chat

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
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.soukmar.app.data.remote.dto.MessageDto
import com.soukmar.app.ui.model.formatPriceParts
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

private val quickReplies = listOf("Toujours disponible ?", "Dernier prix ?", "Toujours intéressé(e) ?", "Merci !")

@Composable
fun ChatScreen(
    conversationId: String,
    onBack: () -> Unit,
    onOpenListing: (String) -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    LaunchedEffect(conversationId) { viewModel.load(conversationId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (viewModel.conversation != null) {
                        ChatHeaderContent(viewModel, onOpenListing)
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour") } },
                actions = {
                    if (viewModel.conversation != null) {
                        IconButton(onClick = { viewModel.reportOpen = true }) {
                            Icon(Icons.Filled.Flag, contentDescription = "Signaler", tint = TextMuted)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                viewModel.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
                viewModel.loadError || viewModel.conversation == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Conversation introuvable.", color = TextMuted)
                }
                else -> ChatContent(viewModel)
            }
        }
    }

    if (viewModel.confirmCancelReservation) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissCancelReservation() },
            title = { Text("Annuler la réservation ?") },
            text = { Text("L'annonce redevient active.") },
            confirmButton = { TextButton(onClick = { viewModel.confirmCancelReservation() }) { Text("Confirmer", color = ErrorColor) } },
            dismissButton = { TextButton(onClick = { viewModel.dismissCancelReservation() }) { Text("Annuler") } }
        )
    }
    if (viewModel.confirmCancelOfferId != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissCancelOffer() },
            title = { Text("Annuler votre offre ?") },
            confirmButton = { TextButton(onClick = { viewModel.confirmCancelOffer() }) { Text("Confirmer", color = ErrorColor) } },
            dismissButton = { TextButton(onClick = { viewModel.dismissCancelOffer() }) { Text("Annuler") } }
        )
    }
    if (viewModel.reportOpen) {
        ReportDialog(viewModel)
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
                Text(viewModel.partnerName(), fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (viewModel.partnerTyping) {
                    Text("en train d'écrire…", fontSize = 11.sp, color = Primary)
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
                Text("🔒 Cette annonce est réservée", fontSize = 13.sp, color = Gold, modifier = Modifier.weight(1f))
                TextButton(onClick = { viewModel.requestCancelReservation() }) { Text("Annuler", fontSize = 12.sp) }
            }
        }

        if (viewModel.reportSubmitted) {
            Text(
                "✅ Signalement envoyé, merci.",
                color = SuccessColor,
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
            )
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

        if (viewModel.showOfferInput) {
            Row(
                modifier = Modifier.fillMaxWidth().background(PrimaryLight).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("💰", fontSize = 16.sp)
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = viewModel.offerAmount,
                    onValueChange = { if (it.all { c -> c.isDigit() }) viewModel.offerAmount = it },
                    placeholder = { Text("Montant en MAD") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, cursorColor = Primary)
                )
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = { viewModel.sendOffer() }, enabled = viewModel.offerAmount.isNotBlank()) { Text("Envoyer") }
                TextButton(onClick = { viewModel.showOfferInput = false }) { Text("Annuler") }
            }
        }

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
                Text("💰", fontSize = 20.sp)
            }
            OutlinedTextField(
                value = viewModel.messageText,
                onValueChange = { viewModel.messageText = it; viewModel.onTyping() },
                placeholder = { Text("Écrivez un message…") },
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
private fun TextBubble(msg: MessageDto, mine: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (mine) Alignment.End else Alignment.Start
    ) {
        Text(formatMsgTime(msg.createdAt), fontSize = 10.sp, color = TextMuted)
        Box(
            modifier = Modifier
                .background(if (mine) Primary else WhiteColor, RoundedCornerShape(14.dp))
                .border(if (mine) 0.dp else 1.dp, BorderColor, RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxWidth(0.8f)
        ) {
            Text(msg.content, color = if (mine) Color.White else TextPrimary, fontSize = 14.sp)
        }
    }
}

@Composable
private fun OfferBubble(
    msg: MessageDto,
    mine: Boolean,
    canRespond: Boolean,
    canCancel: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onCancel: () -> Unit
) {
    val amountText = msg.offerAmount?.let { formatPriceParts(it).first } ?: "—"
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = if (mine) Alignment.End else Alignment.Start) {
        Text(formatMsgTime(msg.createdAt), fontSize = 10.sp, color = TextMuted)
        Column(
            modifier = Modifier
                .background(GoldLight, RoundedCornerShape(14.dp))
                .border(1.dp, Gold, RoundedCornerShape(14.dp))
                .padding(12.dp)
                .fillMaxWidth(0.75f)
        ) {
            Text("💰 Offre de prix", fontSize = 11.sp, color = Gold, fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(amountText, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(Modifier.width(4.dp))
                Text("MAD", fontSize = 11.sp, color = TextMuted)
            }
            when (msg.offerStatus) {
                "PENDING" -> Text("⏳ En attente", fontSize = 11.sp, color = TextMuted)
                "ACCEPTED" -> Text("✅ Acceptée", fontSize = 11.sp, color = SuccessColor)
                "REJECTED" -> if (mine) Text("🚫 Annulée", fontSize = 11.sp, color = ErrorColor) else Text("❌ Refusée", fontSize = 11.sp, color = ErrorColor)
            }
            if (canRespond) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onAccept, colors = ButtonDefaults.buttonColors(containerColor = SuccessColor), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                        Text("✅ Accepter", fontSize = 12.sp)
                    }
                    Button(onClick = onReject, colors = ButtonDefaults.buttonColors(containerColor = ErrorColor), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                        Text("❌ Refuser", fontSize = 12.sp)
                    }
                }
            }
            if (canCancel) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onCancel, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                    Text("🚫 Annuler mon offre", fontSize = 12.sp)
                }
            }
        }
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
                Text(if (viewModel.reportSubmitting) "Envoi…" else "Envoyer")
            }
        },
        dismissButton = { TextButton(onClick = { viewModel.cancelReport() }) { Text("Annuler") } }
    )
}

private val msgTimeFormatter = DateTimeFormatter.ofPattern("dd.MM. HH:mm").withZone(ZoneId.systemDefault())

private fun formatMsgTime(iso: String): String {
    return try {
        msgTimeFormatter.format(Instant.parse(iso))
    } catch (e: DateTimeParseException) {
        ""
    }
}
