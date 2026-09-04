@file:OptIn(ExperimentalMaterial3Api::class)

package com.soukmar.app.ui.screens.notifications

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.soukmar.app.data.i18n.I18nRepository
import com.soukmar.app.data.remote.dto.NotificationDto
import com.soukmar.app.ui.i18n.LocalI18n
import com.soukmar.app.ui.i18n.t
import com.soukmar.app.ui.i18n.timeAgoT
import com.soukmar.app.ui.theme.Primary
import com.soukmar.app.ui.theme.PrimaryLight
import com.soukmar.app.ui.theme.TextMuted
import com.soukmar.app.ui.theme.TextPrimary
import com.soukmar.app.ui.theme.WhiteColor

@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onOpenChat: (String) -> Unit,
    onOpenListing: (String) -> Unit,
    onOpenProfil: () -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(t("notifications.title")) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour") } },
                actions = {
                    if (viewModel.hasUnread) {
                        TextButton(onClick = { viewModel.markAllRead() }) { Text(t("notifications.mark_all_read")) }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                viewModel.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
                viewModel.notifications.isEmpty() -> EmptyState()
                else -> LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(viewModel.notifications, key = { it.id }) { n ->
                        NotificationRow(
                            n = n,
                            onClick = {
                                viewModel.markRead(n)
                                when {
                                    (n.type == "NEW_INQUIRY" || n.type == "NEW_REPLY" || n.type == "NEW_MESSAGE") && n.conversationId != null ->
                                        onOpenChat(n.conversationId)
                                    n.type == "NEW_REVIEW" -> onOpenProfil()
                                    n.type == "SAVED_SEARCH_MATCH" && n.listingId != null -> onOpenListing(n.listingId)
                                    else -> { /* REPORT_RESOLVED and anything else: stay put */ }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun templateFor(n: NotificationDto, i18n: I18nRepository): String {
    val name = n.actorName ?: ""
    return when (n.type) {
        "NEW_INQUIRY" -> i18n.t("notifications.new_inquiry", mapOf("name" to name))
        "NEW_REPLY" -> i18n.t("notifications.new_reply", mapOf("name" to name))
        "NEW_MESSAGE" -> i18n.t("notifications.new_message", mapOf("name" to name))
        "NEW_REVIEW" -> i18n.t("notifications.new_review", mapOf("name" to name))
        "SAVED_SEARCH_MATCH" -> i18n.t("notifications.saved_search_match", mapOf("name" to name))
        "REPORT_RESOLVED" -> i18n.t("notifications.report_resolved")
        else -> "Nouvelle notification."
    }
}

@Composable
private fun NotificationRow(n: NotificationDto, onClick: () -> Unit) {
    val i18n = LocalI18n.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (n.isRead) WhiteColor else PrimaryLight, androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        if (!n.isRead) {
            Box(modifier = Modifier.padding(top = 5.dp).size(8.dp).background(Primary, CircleShape))
            Spacer(Modifier.width(10.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(templateFor(n, i18n), color = TextPrimary, fontSize = 14.sp, fontWeight = if (n.isRead) FontWeight.Normal else FontWeight.SemiBold)
            n.listingTitle?.let {
                Spacer(Modifier.height(4.dp))
                Text("📌 $it", color = TextMuted, fontSize = 12.sp)
            }
            Spacer(Modifier.height(4.dp))
            Text(timeAgoT(n.createdAt), color = TextMuted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🔔", fontSize = 40.sp)
        Spacer(Modifier.height(12.dp))
        Text(t("notifications.empty"), style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        Spacer(Modifier.height(4.dp))
        Text(
            t("notifications.empty_sub"),
            color = TextMuted,
            textAlign = TextAlign.Center
        )
    }
}
