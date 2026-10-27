@file:OptIn(ExperimentalMaterial3Api::class)

package com.soukmar.app.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soukmar.app.data.remote.dto.AdminUserDto
import com.soukmar.app.data.remote.dto.ListingDto
import com.soukmar.app.ui.i18n.LocalI18n
import com.soukmar.app.ui.i18n.formatPricePartsT
import com.soukmar.app.ui.i18n.t
import com.soukmar.app.ui.i18n.tCatalog
import com.soukmar.app.ui.model.CATEGORIES
import com.soukmar.app.ui.model.flagEmoji
import com.soukmar.app.ui.theme.BorderColor
import com.soukmar.app.ui.theme.ErrorColor
import com.soukmar.app.ui.theme.Gold
import com.soukmar.app.ui.theme.Primary
import com.soukmar.app.ui.theme.Secondary
import com.soukmar.app.ui.theme.SuccessColor
import com.soukmar.app.ui.theme.TextMuted
import com.soukmar.app.ui.theme.TextPrimary
import com.soukmar.app.ui.theme.WhiteColor
import java.util.Locale

private val LISTING_FILTERS = listOf("ALL", "ACTIVE", "PENDING", "CHANGES_REQUESTED", "REJECTED", "SUSPENDED", "SOLD", "RESERVED")
private val SEVERITY_FILTERS = listOf("ALL", "HIGH", "MEDIUM", "LOW")
private val ROLES = listOf("USER", "MODERATOR", "ADMIN")
/** Statuses of the review workflow: changed only in the moderation tab, never by the plain "publish" button. */
private val REVIEW_STATUSES = listOf("PENDING", "CHANGES_REQUESTED", "REJECTED", "SUSPENDED")

/** Name of a country in the app language (ISO code in, "Marokko" out). */
@Composable
private fun countryLabel(code: String?): String {
    val lang = LocalI18n.current.currentLang
    return when (code) {
        null -> ""
        "ZZ" -> t("admin.unknown_country")
        else -> Locale("", code).getDisplayCountry(Locale(lang)).ifBlank { code }
    }
}

@Composable
private fun money(price: Double, currency: String): String =
    formatPricePartsT(price, currency).let { "${it.first} ${it.second}".trim() }

@Composable
private fun statusLabel(status: String): String = t("annonces.${status.lowercase()}")

/** Switch between the browsing country and every country (web: the header's scope buttons). */
@Composable
fun AdminScopeBar(vm: AdminDashboardViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth().background(WhiteColor).padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = !vm.allCountries,
            onClick = { vm.setScope(false) },
            label = { Text("${flagEmoji(vm.scopeCountry)} ${countryLabel(vm.scopeCountry)}") }
        )
        FilterChip(
            selected = vm.allCountries,
            onClick = { vm.setScope(true) },
            label = { Text("🌐 ${t("admin.scope_all")}") }
        )
    }
}

@Composable
private fun FilterRow(options: List<String>, selected: String, label: @Composable (String) -> String, onSelect: (String) -> Unit) {
    LazyRow(contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(options) { o -> FilterChip(selected = selected == o, onClick = { onSelect(o) }, label = { Text(label(o)) }) }
    }
}

@Composable
private fun Kpi(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(12.dp)).background(WhiteColor)
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp)).padding(12.dp)
    ) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text(label, fontSize = 12.sp, color = TextMuted)
    }
}

@Composable
private fun Bar(label: String, value: Int, max: Int, color: Color = Primary) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(0.4f), fontSize = 13.sp, color = TextPrimary, maxLines = 1)
        Box(Modifier.weight(0.45f).height(8.dp).clip(RoundedCornerShape(4.dp)).background(BorderColor)) {
            Box(Modifier.fillMaxWidth(if (max > 0) (value.toFloat() / max).coerceIn(0f, 1f) else 0f).fillMaxHeight().background(color))
        }
        Text(value.toString(), modifier = Modifier.weight(0.15f).padding(start = 8.dp), fontSize = 12.sp, color = TextMuted)
    }
}

@Composable
private fun Card(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(WhiteColor)
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp)).padding(12.dp),
        content = content
    )
}

@Composable
private fun Empty(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(text, color = TextMuted) }
}

@Composable
fun AdminOverviewPanel(vm: AdminDashboardViewModel) {
    val total = vm.listings.size
    val active = vm.listings.count { it.status == "ACTIVE" }
    val pending = vm.listings.count { it.status == "PENDING" }
    val premium = vm.listings.count { it.isPremium }
    LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Kpi(t("admin.kpi_total"), "$total", Modifier.weight(1f))
                Kpi(t("admin.kpi_active"), "$active", Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Kpi(t("admin.kpi_pending"), "$pending", Modifier.weight(1f))
                Kpi(t("admin.kpi_users"), "${vm.users.size}", Modifier.weight(1f))
                Kpi(t("admin.kpi_premium"), "$premium", Modifier.weight(1f))
            }
        }
        item {
            Card {
                Text(t("admin.cat_breakdown"), fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(Modifier.height(6.dp))
                val counts = vm.listings.groupingBy { it.category }.eachCount()
                val rows = CATEGORIES.map { it to (counts[it.value] ?: 0) }.sortedByDescending { it.second }
                rows.forEach { (c, n) -> Bar(tCatalog("cats.${c.value}", c.value), n, rows.firstOrNull()?.second ?: 0) }
            }
        }
        item {
            Card {
                Text(t("admin.activity_title"), fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(Modifier.height(6.dp))
                if (vm.activity.isEmpty()) Text(t("admin.activity_empty"), color = TextMuted, fontSize = 13.sp)
                vm.activity.forEach { a ->
                    Column(Modifier.padding(vertical = 5.dp)) {
                        Text(
                            t("admin.activity_${a.type}") + (a.country?.let { " · ${flagEmoji(it)}" } ?: ""),
                            fontSize = 11.sp, color = Primary, fontWeight = FontWeight.SemiBold
                        )
                        Text(a.title, fontSize = 14.sp, color = TextPrimary)
                        a.detail?.let { Text(it, fontSize = 12.sp, color = TextMuted) }
                        Text(a.at.take(16).replace('T', ' '), fontSize = 11.sp, color = TextMuted)
                    }
                    HorizontalDivider(color = BorderColor)
                }
            }
        }
    }
}

@Composable
fun AdminListingsPanel(vm: AdminDashboardViewModel, onOpenListing: (String) -> Unit) {
    var toDelete by remember { mutableStateOf<ListingDto?>(null) }
    Column {
        FilterRow(LISTING_FILTERS, vm.listingFilter, { if (it == "ALL") t("admin.filter_all") else statusLabel(it) }) { vm.listingFilter = it }
        vm.actionError?.let { Text(it, color = ErrorColor, modifier = Modifier.padding(horizontal = 12.dp), fontSize = 13.sp) }
        if (vm.filteredListings.isEmpty()) {
            Empty(t("admin.empty_listings"))
        } else LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(vm.filteredListings, key = { it.id }) { l ->
                Card(Modifier.clickable { onOpenListing(l.id) }) {
                    Text(l.title, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 2)
                    Text(
                        "${l.user?.name ?: ""} · ${l.city} · ${flagEmoji(l.country)}" + (l.price?.let { " · " + money(it, l.currency) } ?: ""),
                        fontSize = 12.sp, color = TextMuted
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(statusLabel(l.status), fontSize = 12.sp, color = Secondary, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.weight(1f))
                        if (l.status != "ACTIVE" && l.status !in REVIEW_STATUSES) {
                            TextButton(onClick = { vm.setListingStatus(l, "ACTIVE") }) { Text(t("annonces.active")) }
                        }
                        TextButton(onClick = { vm.togglePremium(l) }) {
                            Text(if (l.isPremium) "★ Premium" else "☆ Premium", color = if (l.isPremium) Gold else TextMuted)
                        }
                        TextButton(onClick = { toDelete = l }) { Text("✕", color = ErrorColor) }
                    }
                }
            }
        }
    }
    toDelete?.let { l ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            text = { Text(t("admin.confirm_delete_listing", "title" to l.title)) },
            confirmButton = { TextButton(onClick = { vm.deleteListing(l); toDelete = null }) { Text("OK", color = ErrorColor) } },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text(t("common.cancel")) } }
        )
    }
}

@Composable
fun AdminUsersPanel(vm: AdminDashboardViewModel) {
    Column {
        OutlinedTextField(
            value = vm.userSearch, onValueChange = { vm.userSearch = it },
            placeholder = { Text(t("admin.search_users")) }, singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        )
        vm.actionError?.let { Text(it, color = ErrorColor, modifier = Modifier.padding(horizontal = 12.dp), fontSize = 13.sp) }
        if (vm.filteredUsers.isEmpty()) {
            Empty(t("admin.empty_users"))
        } else LazyColumn(contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(vm.filteredUsers, key = { it.id }) { u -> UserCard(u) { role -> vm.setUserRole(u, role) } }
        }
    }
}

@Composable
private fun UserCard(user: AdminUserDto, onRole: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Card {
        Text(user.name, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Text(user.email, fontSize = 12.sp, color = TextMuted)
        Text(
            listOfNotNull(user.city, user.country?.let { "${flagEmoji(it)} ${countryLabel(it)}" }, "${user._count?.listings ?: 0} ${t("admin.col_listings")}").joinToString(" · "),
            fontSize = 12.sp, color = TextMuted
        )
        Spacer(Modifier.height(4.dp))
        Box {
            AssistChip(onClick = { open = true }, label = { Text(roleLabel(user.role)) })
            DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                ROLES.forEach { r -> DropdownMenuItem(text = { Text(roleLabel(r)) }, onClick = { open = false; if (r != user.role) onRole(r) }) }
            }
        }
    }
}

@Composable
private fun roleLabel(role: String): String = when (role) {
    "ADMIN" -> t("profil.role_admin")
    "MODERATOR" -> t("profil.role_mod")
    else -> t("profil.role_user")
}

@Composable
fun AdminVisitorsPanel(vm: AdminDashboardViewModel) {
    val a = vm.analytics
    if (a == null) { Empty(t("admin.activity_empty")); return }
    LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Kpi(t("admin.visitors_active_now"), "${a.activeNow.total}", Modifier.weight(1f))
                Kpi(t("admin.visitors_today"), "${a.today.total}", Modifier.weight(1f))
                Kpi(t("admin.visitors_by_country"), "${a.byCountry.size}", Modifier.weight(1f))
            }
        }
        item {
            Card {
                Text(t("admin.visitors_daily"), fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
                val max = (a.daily.maxOfOrNull { it.visitors } ?: 0).coerceAtLeast(1)
                Row(Modifier.fillMaxWidth().height(90.dp), horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.Bottom) {
                    a.daily.forEach { d -> Box(Modifier.weight(1f).fillMaxHeight((d.visitors.toFloat() / max).coerceAtLeast(0.02f)).background(Primary)) }
                }
                Text(t("admin.visitors_period", "days" to "${a.days}"), fontSize = 11.sp, color = TextMuted)
            }
        }
        item {
            Card {
                Text(t("admin.visitors_by_country"), fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(Modifier.height(6.dp))
                val max = a.byCountry.maxOfOrNull { it.visitors } ?: 0
                a.byCountry.forEach { c -> Bar((if (c.country == "ZZ") "" else flagEmoji(c.country) + " ") + countryLabel(c.country), c.visitors, max, Secondary) }
            }
        }
        item { Text(t("admin.visitors_privacy"), fontSize = 11.sp, color = TextMuted) }
    }
}

@Composable
fun AdminRevenuePanel(vm: AdminDashboardViewModel) {
    val premium = vm.listings.filter { it.isPremium }
    val byMonth = IntArray(12).also { arr -> premium.forEach { l -> l.createdAt.substringAfter('-').take(2).toIntOrNull()?.let { if (it in 1..12) arr[it - 1] += 99 } } }
    LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Kpi(t("admin.revenue_total"), money(premium.size * 99.0, vm.revenueCurrency), Modifier.weight(1f))
                Kpi(t("admin.revenue_active"), "${premium.size}", Modifier.weight(1f))
            }
        }
        item {
            Card {
                Text(t("admin.revenue_chart", "currency" to vm.revenueCurrency), fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
                val max = (byMonth.maxOrNull() ?: 0).coerceAtLeast(1)
                Row(Modifier.fillMaxWidth().height(90.dp), horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.Bottom) {
                    byMonth.forEach { v -> Box(Modifier.weight(1f).fillMaxHeight((v.toFloat() / max).coerceAtLeast(0.02f)).background(Gold)) }
                }
            }
        }
        if (premium.isEmpty()) item { Text(t("admin.empty_premium"), color = TextMuted) }
        items(premium, key = { it.id }) { l ->
            Card {
                Text(l.title, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 2)
                Text("${l.user?.name ?: ""} · ${money(99.0, vm.revenueCurrency)}", fontSize = 12.sp, color = TextMuted)
            }
        }
    }
}

@Composable
fun AdminSecurityPanel(vm: AdminDashboardViewModel) {
    Column {
        Text(t("admin.security_sub"), fontSize = 12.sp, color = TextMuted, modifier = Modifier.padding(12.dp, 8.dp, 12.dp, 0.dp))
        FilterRow(SEVERITY_FILTERS, vm.severityFilter, { if (it == "ALL") t("admin.filter_all") else it }) { vm.severityFilter = it }
        if (vm.filteredSecurity.isEmpty()) {
            Empty(t("admin.security_empty"))
        } else LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(vm.filteredSecurity, key = { it.id }) { e ->
                Card {
                    val color = when (e.severity) { "HIGH" -> ErrorColor; "MEDIUM" -> Gold; else -> SuccessColor }
                    val key = "admin.sec_${e.type.lowercase()}"
                    val label = t(key).let { if (it == key) e.type else it }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(e.severity, fontSize = 11.sp, color = color, fontWeight = FontWeight.Bold)
                        Text(label, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 14.sp)
                    }
                    e.detail?.let { Text(it, fontSize = 12.sp, color = TextMuted) }
                    Text(
                        listOfNotNull(e.user?.let { "${t("admin.security_user")}: ${it.name}" }, e.ip, e.createdAt.take(16).replace('T', ' ')).joinToString(" · "),
                        fontSize = 11.sp, color = TextMuted
                    )
                }
            }
        }
    }
}
