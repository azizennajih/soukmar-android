@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.soukmar.app.ui.screens.listingdetail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.soukmar.app.data.remote.dto.ListingAttributeValueDto
import com.soukmar.app.ui.i18n.t
import com.soukmar.app.ui.i18n.tCatalog
import com.soukmar.app.ui.i18n.timeAgoT
import com.soukmar.app.ui.model.categoryConfig
import com.soukmar.app.ui.model.formatPriceParts
import com.soukmar.app.ui.theme.BorderColor
import com.soukmar.app.ui.theme.ErrorColor
import com.soukmar.app.ui.theme.Primary
import com.soukmar.app.ui.theme.SuccessColor
import com.soukmar.app.ui.theme.TextMuted
import com.soukmar.app.ui.theme.TextPrimary
import com.soukmar.app.ui.theme.WhiteColor

@Composable
fun ListingDetailScreen(
    listingId: String,
    onBack: () -> Unit,
    onRequireLogin: () -> Unit,
    onOpenChat: (String) -> Unit,
    onOpenSeller: (String) -> Unit,
    viewModel: ListingDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(listingId) { viewModel.load(listingId) }
    LaunchedEffect(viewModel.chatMessage) {
        viewModel.chatMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearChatMessage()
        }
    }
    LaunchedEffect(viewModel.navigateToChatId) {
        viewModel.navigateToChatId?.let {
            onOpenChat(it)
            viewModel.clearNavigateToChatId()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Annonce") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Retour") } },
                actions = {
                    if (viewModel.isLoggedIn) {
                        IconButton(onClick = { viewModel.toggleFavorite() }) {
                            Icon(
                                if (viewModel.favorited) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = t(if (viewModel.favorited) "listing.fav_remove" else "listing.fav_add"),
                                tint = if (viewModel.favorited) Primary else TextPrimary
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        when {
            viewModel.loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
            viewModel.loadError || viewModel.listing == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(t("listing.not_found"), color = TextMuted)
            }
            else -> {
                val listing = viewModel.listing!!
                Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
                    ImageGallery(images = listing.images)

                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(listing.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(2.dp))
                            Text(listing.city, color = TextMuted, fontSize = 13.sp)
                            Spacer(Modifier.width(10.dp))
                            Text(timeAgoT(listing.createdAt), color = TextMuted, fontSize = 13.sp)
                            Spacer(Modifier.width(10.dp))
                            Icon(Icons.Filled.RemoveRedEye, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(2.dp))
                            Text("${listing.views}", color = TextMuted, fontSize = 13.sp)
                        }

                        Spacer(Modifier.height(14.dp))
                        val priceParts = listing.price?.let { formatPriceParts(it, listing.currency) }
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(priceParts?.first ?: t("listing.negotiate"), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            if (priceParts != null) {
                                Spacer(Modifier.width(6.dp))
                                Text(priceParts.second, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextMuted)
                            }
                        }
                        viewModel.priceComparisonPct?.let { pct ->
                            Spacer(Modifier.height(4.dp))
                            val good = pct < 0
                            Text(
                                if (good) "📉 ${t("listing.price_below", "pct" to (-pct).toString())}" else "📈 ${t("listing.price_above", "pct" to pct.toString())}",
                                color = if (good) SuccessColor else if (pct > 10) ErrorColor else TextMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        categoryConfig(listing.category)?.let { cat ->
                            Spacer(Modifier.height(10.dp))
                            Box(
                                modifier = Modifier.background(cat.bg, RoundedCornerShape(999.dp)).padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("${cat.emoji} ${tCatalog("cats.${cat.value}", cat.value)}", color = cat.fg, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(Modifier.height(18.dp))
                        Text(t("listing.description"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(Modifier.height(6.dp))
                        Text(listing.description, color = TextPrimary, fontSize = 14.sp, lineHeight = 20.sp)

                        val specs = listing.attributeValues.filter { it.attributeDefinition != null }
                            .sortedBy { it.attributeDefinition!!.sortOrder }
                        if (specs.isNotEmpty()) {
                            Spacer(Modifier.height(18.dp))
                            Text(t("listing.specs_title"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(Modifier.height(8.dp))
                            Column(
                                modifier = Modifier.fillMaxWidth().border(1.dp, BorderColor, RoundedCornerShape(12.dp)).padding(4.dp)
                            ) {
                                specs.forEach { SpecRow(it) }
                            }
                        }

                        Spacer(Modifier.height(20.dp))
                        ContactCard(
                            phone = listing.phone,
                            whatsapp = listing.whatsapp,
                            isLoggedIn = viewModel.isLoggedIn,
                            chatStarting = viewModel.chatStarting,
                            onChat = { viewModel.startChat() },
                            onLogin = onRequireLogin,
                            onCallWhatsapp = { number ->
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$number")))
                            }
                        )

                        listing.user?.let { seller ->
                            Spacer(Modifier.height(14.dp))
                            SellerCard(
                                name = seller.name,
                                city = seller.city,
                                onSeeListings = { onOpenSeller(listing.userId) }
                            )
                        }

                        if (viewModel.isLoggedIn && viewModel.currentUserId != listing.userId) {
                            if (viewModel.canReview) {
                                Spacer(Modifier.height(14.dp))
                                ReviewCard(viewModel)
                            }
                            if (viewModel.reviewSubmitted) {
                                Spacer(Modifier.height(10.dp))
                                Text("✅ ${t("listing.review_thanks")}", color = SuccessColor, fontSize = 13.sp)
                            }
                            Spacer(Modifier.height(14.dp))
                            ReportSection(viewModel)
                        }

                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageGallery(images: List<String>) {
    if (images.isEmpty()) {
        Box(Modifier.fillMaxWidth().height(260.dp).background(Color(0xFFF1F5F9)), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.PhotoCamera, contentDescription = null, tint = TextMuted)
        }
        return
    }
    val pagerState = rememberPagerState(pageCount = { images.size })
    Column {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth().height(260.dp)) { page ->
            AsyncImage(
                model = images[page],
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        if (images.size > 1) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                images.indices.forEach { i ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (i == pagerState.currentPage) 8.dp else 6.dp)
                            .background(if (i == pagerState.currentPage) Primary else BorderColor, RoundedCornerShape(50))
                    )
                }
            }
        }
    }
}

@Composable
private fun SpecRow(av: ListingAttributeValueDto) {
    val def = av.attributeDefinition!!
    val label = tCatalog("attrs.${def.code}", def.code)
    val value = when (def.type) {
        "BOOLEAN" -> if (av.valueBoolean == true) t("common.yes") else t("common.no")
        "NUMBER" -> av.valueNumber?.let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() } ?: ""
        // SELECT stores a fixed option code (needs a catalog lookup); TEXT is
        // free-form user input and must be shown exactly as entered.
        "SELECT" -> av.valueText?.let { tCatalog("attrs.opts.$it", it) } ?: ""
        else -> av.valueText ?: ""
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextMuted, fontSize = 13.sp)
        Text(value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ContactCard(
    phone: String?,
    whatsapp: String?,
    isLoggedIn: Boolean,
    chatStarting: Boolean,
    onChat: () -> Unit,
    onLogin: () -> Unit,
    onCallWhatsapp: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().background(WhiteColor, RoundedCornerShape(14.dp)).border(1.dp, BorderColor, RoundedCornerShape(14.dp)).padding(14.dp)
    ) {
        if (!isLoggedIn) {
            Button(onClick = onLogin, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                Text("🔐 ${t("listing.login_contact")}")
            }
            return@Column
        }
        phone?.let {
            Row(
                modifier = Modifier.fillMaxWidth().background(SuccessColor.copy(alpha = 0.08f), RoundedCornerShape(10.dp)).padding(12.dp)
            ) { Text("📞 $it", color = SuccessColor, fontWeight = FontWeight.SemiBold) }
            Spacer(Modifier.height(8.dp))
        }
        whatsapp?.let { number ->
            OutlinedButton(onClick = { onCallWhatsapp(number) }, modifier = Modifier.fillMaxWidth()) {
                Text("💬 WhatsApp · $number")
            }
            Spacer(Modifier.height(8.dp))
        }
        Button(
            onClick = onChat,
            enabled = !chatStarting,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            if (chatStarting) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
            else Text("💬 ${t("listing.contact")}")
        }
    }
}

@Composable
private fun SellerCard(name: String, city: String?, onSeeListings: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().background(WhiteColor, RoundedCornerShape(14.dp)).border(1.dp, BorderColor, RoundedCornerShape(14.dp)).padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).background(Primary, RoundedCornerShape(999.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(name.firstOrNull()?.uppercase() ?: "?", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(name, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                city?.let { Text("📍 $it", color = TextMuted, fontSize = 12.sp) }
            }
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onSeeListings, modifier = Modifier.fillMaxWidth()) {
            Text(t("listing.seller_listings"), fontSize = 13.sp)
        }
    }
}

@Composable
private fun ReviewCard(viewModel: ListingDetailViewModel) {
    Column(
        modifier = Modifier.fillMaxWidth().background(WhiteColor, RoundedCornerShape(14.dp)).border(1.dp, BorderColor, RoundedCornerShape(14.dp)).padding(14.dp)
    ) {
        if (!viewModel.showReviewForm) {
            OutlinedButton(onClick = { viewModel.showReviewForm = true }, modifier = Modifier.fillMaxWidth()) {
                Text("⭐ ${t("listing.leave_review")}")
            }
        } else {
            Text(t("listing.leave_review"), fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            Row {
                (1..5).forEach { s ->
                    Text(
                        "★",
                        fontSize = 26.sp,
                        color = if (s <= viewModel.reviewRating) Primary else BorderColor,
                        modifier = Modifier.clickable { viewModel.reviewRating = s }.padding(end = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = viewModel.reviewComment,
                onValueChange = { viewModel.reviewComment = it },
                placeholder = { Text(t("listing.review_placeholder")) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { viewModel.submitReview() },
                enabled = !viewModel.reviewSubmitting,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(if (viewModel.reviewSubmitting) "Envoi…" else t("listing.submit_review"))
            }
        }
    }
}

@Composable
private fun ReportSection(viewModel: ListingDetailViewModel) {
    if (viewModel.reportSubmitted) {
        Text("✅ ${t("report.thanks")}", color = SuccessColor, fontSize = 13.sp)
        return
    }
    if (!viewModel.reportOpen) {
        TextButton(onClick = { viewModel.reportOpen = true }) {
            Text("🚩 ${t("listing.report")}", color = TextMuted)
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth().background(WhiteColor, RoundedCornerShape(14.dp)).border(1.dp, BorderColor, RoundedCornerShape(14.dp)).padding(14.dp)
        ) {
            Text(t("report.form_title"), fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = viewModel.reportReason,
                onValueChange = { viewModel.reportReason = it },
                placeholder = { Text(t("report.placeholder")) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            viewModel.reportError?.let {
                Spacer(Modifier.height(6.dp))
                Text(it, color = ErrorColor, fontSize = 12.sp)
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { viewModel.cancelReport() }) { Text(t("common.cancel")) }
                Button(
                    onClick = { viewModel.submitReport() },
                    enabled = !viewModel.reportSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text(if (viewModel.reportSubmitting) "Envoi…" else t("report.submit"))
                }
            }
        }
    }
}
