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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Star
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
import com.soukmar.app.data.remote.dto.ListingDto
import com.soukmar.app.ui.components.ListingCard
import com.soukmar.app.ui.components.ListingsMapView
import com.soukmar.app.ui.components.VerifiedBadge
import com.soukmar.app.ui.i18n.cityLabelT
import com.soukmar.app.ui.i18n.dateAttrT
import com.soukmar.app.ui.i18n.t
import com.soukmar.app.ui.i18n.tCatalog
import com.soukmar.app.ui.i18n.timeAgoT
import com.soukmar.app.ui.model.CategoryIcon
import com.soukmar.app.ui.model.categoryConfig
import com.soukmar.app.ui.model.countryFlag
import com.soukmar.app.ui.i18n.formatPricePartsT
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
    onOpenListing: (String) -> Unit,
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
                title = { Text(viewModel.listing?.let { tCatalog("cats.${it.category}", it.category) } ?: "", style = MaterialTheme.typography.titleLarge, maxLines = 1) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = t("chat.back")) } },
                actions = {
                    if (viewModel.isLoggedIn) {
                        IconButton(onClick = { viewModel.toggleFavorite() }) {
                            Icon(
                                if (viewModel.favorited) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
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
                com.soukmar.app.ui.components.EmptyState(Icons.Outlined.PhotoCamera, t("listing.not_found"))
            }
            else -> {
                val listing = viewModel.listing!!
                Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
                    // The owner and staff can open a listing that is not public yet; say so.
                    if (listing.status == "PENDING" || listing.status == "CHANGES_REQUESTED" || listing.status == "REJECTED" || listing.status == "SUSPENDED") {
                        val statusText = when (listing.status) {
                            "PENDING" -> t("annonces.pending")
                            "CHANGES_REQUESTED" -> t("annonces.changes_requested")
                            "REJECTED" -> t("annonces.rejected")
                            else -> t("annonces.suspended")
                        }
                        Text(
                            t("listing.not_public_notice", "status" to statusText),
                            fontSize = 13.sp,
                            color = TextPrimary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(com.soukmar.app.ui.theme.GoldLight)
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                    ImageGallery(images = listing.images)

                    Column(modifier = Modifier.padding(16.dp)) {
                        val priceParts = listing.price?.let { formatPricePartsT(it, listing.currency) }
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(priceParts?.first ?: t("listing.negotiate"), fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                            if (priceParts != null) {
                                Spacer(Modifier.width(6.dp))
                                Text(priceParts.second, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, modifier = Modifier.padding(bottom = 3.dp))
                            }
                        }
                        viewModel.priceComparisonPct?.let { pct ->
                            Spacer(Modifier.height(4.dp))
                            val good = pct < 0
                            val trendColor = if (good) SuccessColor else if (pct > 10) ErrorColor else TextMuted
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(if (good) Icons.AutoMirrored.Outlined.TrendingDown else Icons.AutoMirrored.Outlined.TrendingUp, contentDescription = null, tint = trendColor, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    if (good) t("listing.price_below", "pct" to (-pct).toString()) else t("listing.price_above", "pct" to pct.toString()),
                                    color = trendColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        Text(listing.title, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(2.dp))
                            Text(cityLabelT(listing.city), color = TextMuted, fontSize = 13.sp)
                            if (listing.country != "MA") {
                                Spacer(Modifier.width(4.dp))
                                Text(countryFlag(listing.country), fontSize = 13.sp)
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(timeAgoT(listing.createdAt), color = TextMuted, fontSize = 13.sp)
                            Spacer(Modifier.width(10.dp))
                            Icon(Icons.Outlined.RemoveRedEye, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(2.dp))
                            Text("${listing.views}", color = TextMuted, fontSize = 13.sp)
                        }


                        categoryConfig(listing.category)?.let { cat ->
                            Spacer(Modifier.height(10.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier.background(com.soukmar.app.ui.theme.SurfaceMuted, RoundedCornerShape(999.dp)).padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                CategoryIcon(category = cat.value, tint = TextPrimary, modifier = Modifier.size(14.dp))
                                Text(tCatalog("cats.${cat.value}", cat.value), color = TextPrimary, style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        Spacer(Modifier.height(18.dp))
                        Text(t("listing.description"), style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Spacer(Modifier.height(6.dp))
                        Text(listing.description, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)

                        // MULTI_SELECT attributes produce one ListingAttributeValue row
                        // per selected option (same attributeDefinitionId) — group them
                        // so SpecRow renders one line per attribute, not one per value.
                        val specGroups = listing.attributeValues.filter { it.attributeDefinition != null }
                            .groupBy { it.attributeDefinitionId }
                            .values
                            .sortedBy { it.first().attributeDefinition!!.sortOrder }
                        if (specGroups.isNotEmpty()) {
                            Spacer(Modifier.height(18.dp))
                            Text(t("listing.specs_title"), style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            Spacer(Modifier.height(8.dp))
                            Column(
                                modifier = Modifier.fillMaxWidth().border(1.dp, BorderColor, RoundedCornerShape(12.dp)).padding(4.dp)
                            ) {
                                specGroups.forEach { SpecRow(it) }
                            }
                        }

                        if (listing.lat != null && listing.lng != null) {
                            Spacer(Modifier.height(18.dp))
                            LocationMapSection(listing)
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
                                // Digits only: the number is user data and must not be able to alter the link.
                                val digits = number.filter { it.isDigit() }
                                if (digits.isNotEmpty()) {
                                    try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$digits"))) } catch (e: Exception) { /* no app or browser */ }
                                }
                            }
                        )

                        listing.user?.let { seller ->
                            Spacer(Modifier.height(14.dp))
                            SellerCard(
                                name = seller.name,
                                city = seller.city,
                                accountType = seller.accountType,
                                emailVerified = seller.emailVerified,
                                phoneVerified = seller.phoneVerified,
                                idVerified = seller.idVerified,
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
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = SuccessColor, modifier = Modifier.size(15.dp))
                                    Text(t("listing.review_thanks"), color = SuccessColor, fontSize = 13.sp)
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                            ReportSection(viewModel)
                        }

                        if (viewModel.similarListings.isNotEmpty()) {
                            Spacer(Modifier.height(20.dp))
                            SimilarListingsSection(viewModel.similarListings, onOpenListing)
                        }

                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

/** Mirrors the web's "Cela pourrait aussi vous intéresser" section (same
 * subcategory first, padded out with the same category) — chunked into
 * pairs of Rows rather than a nested LazyVerticalGrid, since this section
 * lives inside the screen's outer verticalScroll Column and a nested
 * lazy grid there would have unbounded height. */
@Composable
private fun SimilarListingsSection(listings: List<com.soukmar.app.data.remote.dto.ListingDto>, onOpenListing: (String) -> Unit) {
    Column {
        Text(t("listing.similar"), style = MaterialTheme.typography.titleLarge, color = TextPrimary)
        Spacer(Modifier.height(10.dp))
        listings.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { item ->
                    ListingCard(listing = item, onClick = { onOpenListing(item.id) }, modifier = Modifier.weight(1f))
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun ImageGallery(images: List<String>) {
    if (images.isEmpty()) {
        Box(Modifier.fillMaxWidth().aspectRatio(4f / 3f).background(com.soukmar.app.ui.theme.SurfaceMuted), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.PhotoCamera, contentDescription = null, tint = com.soukmar.app.ui.theme.TextSubtle, modifier = Modifier.size(40.dp))
        }
        return
    }
    val pagerState = rememberPagerState(pageCount = { images.size })
    Column {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f).background(com.soukmar.app.ui.theme.SurfaceMuted)) { page ->
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
private fun SpecRow(rows: List<ListingAttributeValueDto>) {
    val av = rows.first()
    val def = av.attributeDefinition!!
    val label = tCatalog("attrs.${def.code}", def.code)
    val value = when (def.type) {
        "BOOLEAN" -> if (av.valueBoolean == true) t("common.yes") else t("common.no")
        "NUMBER" -> av.valueNumber?.let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() } ?: ""
        // SELECT stores a fixed option code (needs a catalog lookup); TEXT is
        // free-form user input and must be shown exactly as entered.
        "SELECT" -> av.valueText?.let { tCatalog("attrs.opts.$it", it) } ?: ""
        // MULTI_SELECT produces one row per selected option — join their labels.
        // (A plain loop, not .joinToString{}'s lambda param, since that param
        // isn't a @Composable-compatible functional type.)
        "MULTI_SELECT" -> {
            val labels = mutableListOf<String>()
            for (row in rows) row.valueText?.let { labels.add(tCatalog("attrs.opts.$it", it)) }
            labels.joinToString(", ")
        }
        "DATE" -> av.valueText?.let { dateAttrT(it) } ?: ""
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
        modifier = Modifier.fillMaxWidth().background(WhiteColor, RoundedCornerShape(16.dp)).border(1.dp, BorderColor, RoundedCornerShape(16.dp)).padding(16.dp)
    ) {
        if (!isLoggedIn) {
            Button(onClick = onLogin, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp), colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                Icon(Icons.Outlined.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(t("listing.login_contact"))
            }
            return@Column
        }
        phone?.let {
            Row(
                modifier = Modifier.fillMaxWidth().background(com.soukmar.app.ui.theme.SuccessLight, RoundedCornerShape(12.dp)).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Phone, contentDescription = null, tint = SuccessColor, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(it, color = SuccessColor, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))
        }
        whatsapp?.let { number ->
            OutlinedButton(onClick = { onCallWhatsapp(number) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("WhatsApp · $number")
            }
            Spacer(Modifier.height(8.dp))
        }
        Button(
            onClick = onChat,
            enabled = !chatStarting,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            if (chatStarting) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
            else {
                Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(t("listing.contact"))
            }
        }
    }
}

@Composable
private fun LocationMapSection(listing: ListingDto) {
    var showMap by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxWidth().background(WhiteColor, RoundedCornerShape(16.dp)).border(1.dp, BorderColor, RoundedCornerShape(16.dp)).padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(t("listing.location_title"), style = MaterialTheme.typography.titleMedium, color = TextPrimary, modifier = Modifier.weight(1f))
            TextButton(onClick = { showMap = !showMap }) {
                Text(t(if (showMap) "listing.hide_map" else "listing.show_map"), fontSize = 12.sp)
            }
        }
        if (showMap) {
            Spacer(Modifier.height(8.dp))
            ListingsMapView(listings = listOf(listing), heightDp = 220, onMarkerClick = {})
        }
    }
}

@Composable
private fun SellerCard(name: String, city: String?, accountType: String?, emailVerified: Boolean, phoneVerified: Boolean, idVerified: Boolean = false, onSeeListings: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().background(WhiteColor, RoundedCornerShape(16.dp)).border(1.dp, BorderColor, RoundedCornerShape(16.dp)).padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).background(Primary, RoundedCornerShape(999.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(name.firstOrNull()?.uppercase() ?: "?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(name, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                city?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = TextMuted, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(2.dp))
                        Text(cityLabelT(it), color = TextMuted, fontSize = 12.sp)
                    }
                }
                accountType?.let {
                    com.soukmar.app.ui.components.AccountTypeLabel(it, modifier = Modifier.padding(top = 2.dp))
                }
                VerifiedBadge(emailVerified, phoneVerified, idVerified, modifier = Modifier.padding(top = 2.dp))
            }
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onSeeListings, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Text(t("listing.seller_listings"), fontSize = 13.sp)
        }
    }
}

@Composable
private fun ReviewCard(viewModel: ListingDetailViewModel) {
    Column(
        modifier = Modifier.fillMaxWidth().background(WhiteColor, RoundedCornerShape(16.dp)).border(1.dp, BorderColor, RoundedCornerShape(16.dp)).padding(16.dp)
    ) {
        if (!viewModel.showReviewForm) {
            OutlinedButton(onClick = { viewModel.showReviewForm = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Icon(Icons.Outlined.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(t("listing.leave_review"))
            }
        } else {
            Text(t("listing.leave_review"), fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            Row {
                (1..5).forEach { s ->
                    Text(
                        "★",
                        fontSize = 32.sp,
                        color = if (s <= viewModel.reviewRating) com.soukmar.app.ui.theme.Accent else BorderColor,
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
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = SuccessColor, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(6.dp))
            Text(t("report.thanks"), color = SuccessColor, fontSize = 13.sp)
        }
        return
    }
    if (!viewModel.reportOpen) {
        TextButton(onClick = { viewModel.reportOpen = true }) {
            Icon(Icons.Outlined.Flag, contentDescription = null, tint = TextMuted, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(6.dp))
            Text(t("listing.report"), color = TextMuted)
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth().background(WhiteColor, RoundedCornerShape(16.dp)).border(1.dp, BorderColor, RoundedCornerShape(16.dp)).padding(16.dp)
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
