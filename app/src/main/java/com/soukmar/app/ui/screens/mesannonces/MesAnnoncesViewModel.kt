package com.soukmar.app.ui.screens.mesannonces

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soukmar.app.data.remote.dto.ListingDto
import com.soukmar.app.data.remote.dto.ListingFunnelDto
import com.soukmar.app.data.remote.dto.ViewStatDayDto
import com.soukmar.app.data.repository.ApiResult
import com.soukmar.app.data.repository.ListingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

enum class SortKey { NEWEST, OLDEST, PRICE_ASC, PRICE_DESC }

@HiltViewModel
class MesAnnoncesViewModel @Inject constructor(
    private val listingRepository: ListingRepository
) : ViewModel() {

    var listings by mutableStateOf<List<ListingDto>>(emptyList())
        private set
    var loading by mutableStateOf(true)
        private set

    var sortBy by mutableStateOf(SortKey.NEWEST)

    val sortedListings: List<ListingDto>
        get() = when (sortBy) {
            SortKey.NEWEST -> listings.sortedByDescending { parseInstantOrNull(it.createdAt) }
            SortKey.OLDEST -> listings.sortedBy { parseInstantOrNull(it.createdAt) }
            SortKey.PRICE_ASC -> listings.sortedBy { it.price ?: Double.MAX_VALUE }
            SortKey.PRICE_DESC -> listings.sortedByDescending { it.price ?: Double.MIN_VALUE }
        }

    var bumpingId by mutableStateOf<String?>(null)
        private set
    var extendingId by mutableStateOf<String?>(null)
        private set
    var statsOpenId by mutableStateOf<String?>(null)
        private set
    var statsData by mutableStateOf<Map<String, List<ViewStatDayDto>>>(emptyMap())
        private set
    var funnelData by mutableStateOf<Map<String, ListingFunnelDto>>(emptyMap())
        private set
    var deleteConfirmId by mutableStateOf<String?>(null)

    var toastMessage by mutableStateOf<String?>(null)
        private set

    fun load() {
        viewModelScope.launch {
            loading = true
            when (val result = listingRepository.getMyListings()) {
                is ApiResult.Success -> listings = result.data
                is ApiResult.Error -> toastMessage = result.message
            }
            loading = false
        }
    }

    fun canBump(listing: ListingDto): Boolean {
        val bumpedAt = listing.bumpedAt ?: return true
        return try {
            ChronoUnit.HOURS.between(Instant.parse(bumpedAt), Instant.now()) >= 24
        } catch (e: Exception) {
            true
        }
    }

    fun bump(listing: ListingDto) {
        if (bumpingId != null || !canBump(listing)) return
        bumpingId = listing.id
        viewModelScope.launch {
            when (val result = listingRepository.bump(listing.id)) {
                is ApiResult.Success -> listings = listings.map { if (it.id == listing.id) it.copy(bumpedAt = result.data.bumpedAt) else it }
                is ApiResult.Error -> toastMessage = result.message
            }
            bumpingId = null
        }
    }

    fun daysUntilExpiry(listing: ListingDto): Long? {
        val expiresAt = listing.expiresAt?.let { parseInstantOrNull(it) } ?: return null
        val ms = expiresAt.toEpochMilli() - System.currentTimeMillis()
        return maxOf(0L, Math.ceil(ms / 86_400_000.0).toLong())
    }

    fun isExpiringSoon(listing: ListingDto): Boolean {
        val days = daysUntilExpiry(listing) ?: return false
        return days <= 7
    }

    fun canExtend(listing: ListingDto): Boolean = listing.expiresAt != null && !listing.expiryExtended

    fun extend(listing: ListingDto) {
        if (extendingId != null || !canExtend(listing)) return
        extendingId = listing.id
        viewModelScope.launch {
            when (val result = listingRepository.extend(listing.id)) {
                is ApiResult.Success -> listings = listings.map {
                    if (it.id == listing.id) it.copy(expiresAt = result.data.expiresAt, expiryExtended = result.data.expiryExtended) else it
                }
                is ApiResult.Error -> toastMessage = result.message
            }
            extendingId = null
        }
    }

    fun toggleReserve(listing: ListingDto) {
        val nextStatus = if (listing.status == "RESERVED") "ACTIVE" else "RESERVED"
        val previous = listings
        listings = listings.map { if (it.id == listing.id) it.copy(status = nextStatus) else it }
        viewModelScope.launch {
            when (listingRepository.updateStatus(listing.id, nextStatus)) {
                is ApiResult.Success -> { /* optimistic value already applied */ }
                is ApiResult.Error -> listings = previous
            }
        }
    }

    fun toggleStats(listing: ListingDto) {
        val id = listing.id
        statsOpenId = if (statsOpenId == id) null else id
        if (statsOpenId == id && !statsData.containsKey(id)) {
            viewModelScope.launch {
                when (val result = listingRepository.getViewStats(id)) {
                    is ApiResult.Success -> statsData = statsData + (id to result.data.days)
                    is ApiResult.Error -> { /* stats panel just stays empty on failure */ }
                }
            }
        }
        if (statsOpenId == id && !funnelData.containsKey(id)) {
            viewModelScope.launch {
                when (val result = listingRepository.getFunnel(id)) {
                    is ApiResult.Success -> funnelData = funnelData + (id to result.data)
                    is ApiResult.Error -> { /* funnel panel just stays empty on failure */ }
                }
            }
        }
    }

    /** Mirrors the web's `funnelPct()`: each step's bar width relative to
     * the top of the funnel (views) — 100% for views itself, shrinking down
     * through favorites/contacts/offers/accepted. Guards against
     * divide-by-zero on a brand-new listing with 0 views. */
    fun funnelPct(id: String, count: Int): Float {
        val views = funnelData[id]?.views ?: 0
        if (views <= 0) return 0f
        return (count.toFloat() / views).coerceIn(0f, 1f)
    }

    fun requestDelete(id: String) { deleteConfirmId = id }
    fun dismissDelete() { deleteConfirmId = null }
    fun confirmDelete() {
        val id = deleteConfirmId ?: return
        deleteConfirmId = null
        viewModelScope.launch {
            when (listingRepository.deleteListing(id)) {
                is ApiResult.Success -> listings = listings.filter { it.id != id }
                is ApiResult.Error -> { /* leave the row in place; the user can retry */ }
            }
        }
    }

    fun clearToast() { toastMessage = null }
}

private fun parseInstantOrNull(value: String): Instant? = try { Instant.parse(value) } catch (e: Exception) { null }
