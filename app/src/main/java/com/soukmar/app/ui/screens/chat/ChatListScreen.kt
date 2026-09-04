@file:OptIn(ExperimentalMaterial3Api::class)

package com.soukmar.app.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.soukmar.app.data.remote.dto.ConversationDto
import com.soukmar.app.data.remote.dto.partnerName
import com.soukmar.app.ui.i18n.t
import com.soukmar.app.ui.theme.BorderColor
import com.soukmar.app.ui.theme.Primary
import com.soukmar.app.ui.theme.TextMuted
import com.soukmar.app.ui.theme.TextPrimary

@Composable
fun ChatListScreen(
    onBack: () -> Unit,
    onOpenConversation: (String) -> Unit,
    viewModel: ChatListViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(t("chat.title")) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = t("chat.back")) } }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                viewModel.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
                viewModel.conversations.isEmpty() -> EmptyConversations()
                else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(viewModel.conversations, key = { it.id }) { conv ->
                        ConversationRow(conv, viewModel.currentUserId, onClick = { onOpenConversation(conv.id) })
                        HorizontalDivider(color = BorderColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyConversations() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("💬", fontSize = 40.sp)
        Spacer(Modifier.height(12.dp))
        Text(t("chat.no_conv"), style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        Spacer(Modifier.height(4.dp))
        Text(t("chat.no_conv_sub"), color = TextMuted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
private fun ConversationRow(conv: ConversationDto, myId: String?, onClick: () -> Unit) {
    val name = conv.partnerName(myId)
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(48.dp).background(Primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(conv.listing.title, color = TextMuted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Text(lastMessagePreview(conv), color = TextMuted, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

private fun lastMessagePreview(conv: ConversationDto): String {
    val last = conv.messages.firstOrNull() ?: return "Aucun message"
    if (last.type == "OFFER") {
        val amount = last.offerAmount?.let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() } ?: ""
        return "Offre: $amount MAD"
    }
    return last.content.take(40)
}
