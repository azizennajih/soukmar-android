@file:OptIn(ExperimentalMaterial3Api::class)

package com.soukmar.app.ui.screens.mesabonnements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PersonAddAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.soukmar.app.data.remote.dto.FollowedUserDto
import com.soukmar.app.ui.components.FollowButton
import com.soukmar.app.ui.components.VerifiedBadge
import com.soukmar.app.ui.i18n.cityLabelT
import com.soukmar.app.ui.i18n.t
import com.soukmar.app.ui.theme.BorderColor
import com.soukmar.app.ui.theme.Primary
import com.soukmar.app.ui.theme.TextMuted
import com.soukmar.app.ui.theme.TextPrimary
import com.soukmar.app.ui.theme.WhiteColor

/** Mirrors the web's "Mes abonnements" page — list of followed sellers/
 * buyers, each row with its own unfollow button. A row is only dropped
 * from the list once the backend confirms the unfollow (see
 * MesAbonnementsViewModel.unfollow()), not optimistically beforehand. */
@Composable
fun MesAbonnementsScreen(
    onBack: () -> Unit,
    onOpenSeller: (String) -> Unit,
    onBrowse: () -> Unit,
    viewModel: MesAbonnementsViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${t("mes_abonnements.title")} (${viewModel.users.size})") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour") } }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                viewModel.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
                viewModel.users.isEmpty() -> EmptyAbonnements(onBrowse)
                else -> LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(viewModel.users, key = { it.id }) { user ->
                        FollowedUserRow(
                            user = user,
                            submitting = user.id in viewModel.submittingIds,
                            onOpenSeller = onOpenSeller,
                            onUnfollow = { viewModel.unfollow(user.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FollowedUserRow(user: FollowedUserDto, submitting: Boolean, onOpenSeller: (String) -> Unit, onUnfollow: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(WhiteColor, RoundedCornerShape(14.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
            .clickable { onOpenSeller(user.id) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(Primary),
            contentAlignment = Alignment.Center
        ) {
            if (user.image != null) {
                AsyncImage(model = user.image, contentDescription = user.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                Text(user.name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(user.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
            VerifiedBadge(user.emailVerified, user.phoneVerified, user.idVerified)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                user.city?.let {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = TextMuted, modifier = Modifier.size(11.dp))
                    Text(cityLabelT(it), color = TextMuted, fontSize = 11.sp)
                }
                Text("${user.activeListingsCount} ${t("seller.listings_title")}", color = TextMuted, fontSize = 11.sp)
            }
        }
        FollowButton(following = true, submitting = submitting, onToggle = onUnfollow)
    }
}

@Composable
private fun EmptyAbonnements(onBrowse: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.PersonAddAlt, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
        Spacer(Modifier.height(12.dp))
        Text(t("mes_abonnements.empty"), style = MaterialTheme.typography.titleMedium, color = TextPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text(t("mes_abonnements.empty_sub"), color = TextMuted, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onBrowse, colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
            Text(t("mes_abonnements.browse_btn"))
        }
    }
}
