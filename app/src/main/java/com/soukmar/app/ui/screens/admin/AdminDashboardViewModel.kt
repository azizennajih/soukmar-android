package com.soukmar.app.ui.screens.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soukmar.app.data.country.CountryRepository
import com.soukmar.app.data.remote.dto.AdminActivityDto
import com.soukmar.app.data.remote.dto.AdminAnalyticsDto
import com.soukmar.app.data.remote.dto.AdminUserDto
import com.soukmar.app.data.remote.dto.ListingDto
import com.soukmar.app.data.remote.dto.SecurityEventDto
import com.soukmar.app.data.repository.AdminRepository
import com.soukmar.app.data.repository.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Overview / listings / users / visitors / revenue / security of the admin dashboard (web: AdminComponent).
 * Everything except the security log follows [allCountries]: the browsing country, or all countries at once. */
@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val repository: AdminRepository,
    private val countryRepository: CountryRepository
) : ViewModel() {
    var allCountries by mutableStateOf(false)
        private set
    val scopeCountry: String get() = countryRepository.country
    private val queryCountry: String? get() = if (allCountries) null else countryRepository.country

    var loading by mutableStateOf(true)
    var listings by mutableStateOf<List<ListingDto>>(emptyList())
    var users by mutableStateOf<List<AdminUserDto>>(emptyList())
    var activity by mutableStateOf<List<AdminActivityDto>>(emptyList())
    var analytics by mutableStateOf<AdminAnalyticsDto?>(null)
    var securityEvents by mutableStateOf<List<SecurityEventDto>>(emptyList())

    var listingFilter by mutableStateOf("ALL")
    var userSearch by mutableStateOf("")
    var severityFilter by mutableStateOf("ALL")
    var actionError by mutableStateOf<String?>(null)
    private var loadedFor: String? = "unset"

    /** Reloads when the scope (or the browsing country) changed since the last load. */
    fun ensureLoaded() {
        val key = (queryCountry ?: "*")
        if (loadedFor == key) return
        loadedFor = key
        reload()
    }

    fun setScope(all: Boolean) {
        if (allCountries == all) return
        allCountries = all
        ensureLoaded()
    }

    fun reload() {
        loading = true
        val c = queryCountry
        viewModelScope.launch {
            (repository.getAllListings(c) as? ApiResult.Success)?.let { listings = it.data } ?: run { listings = emptyList() }
            (repository.getUsers(c) as? ApiResult.Success)?.let { users = it.data } ?: run { users = emptyList() }
            (repository.getActivity(c) as? ApiResult.Success)?.let { activity = it.data } ?: run { activity = emptyList() }
            analytics = (repository.getAnalytics(c) as? ApiResult.Success)?.data
            (repository.getSecurityEvents() as? ApiResult.Success)?.let { securityEvents = it.data }
            loading = false
        }
    }

    val filteredListings: List<ListingDto>
        get() = if (listingFilter == "ALL") listings else listings.filter { it.status == listingFilter }

    val filteredUsers: List<AdminUserDto>
        get() {
            val q = userSearch.trim().lowercase()
            return if (q.isEmpty()) users else users.filter { it.name.lowercase().contains(q) || it.email.lowercase().contains(q) }
        }

    val filteredSecurity: List<SecurityEventDto>
        get() = if (severityFilter == "ALL") securityEvents else securityEvents.filter { it.severity == severityFilter }

    val revenueCurrency: String
        get() = if (allCountries) "EUR" else com.soukmar.app.ui.model.currencyForCountry(countryRepository.country)

    private fun act(block: suspend () -> ApiResult<Boolean>, after: () -> Unit = {}) {
        viewModelScope.launch {
            when (val r = block()) {
                is ApiResult.Success -> { actionError = null; after() }
                is ApiResult.Error -> actionError = r.message
            }
        }
    }

    fun setUserRole(user: AdminUserDto, role: String) = act({ repository.setUserRole(user.id, role) }) {
        users = users.map { if (it.id == user.id) it.copy(role = role) else it }
    }

    fun setListingStatus(listing: ListingDto, status: String) = act({ repository.setListingStatus(listing.id, status) }) {
        listings = listings.map { if (it.id == listing.id) it.copy(status = status) else it }
    }

    fun togglePremium(listing: ListingDto) = act({ repository.setListingPremium(listing.id, !listing.isPremium) }) {
        listings = listings.map { if (it.id == listing.id) it.copy(isPremium = !listing.isPremium) else it }
    }

    fun deleteListing(listing: ListingDto) = act({ repository.deleteListing(listing.id) }) {
        listings = listings.filter { it.id != listing.id }
    }
}
