package com.soukmar.app.ui.screens.boostlisting

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soukmar.app.data.remote.dto.BoostStatusDto
import com.soukmar.app.data.remote.dto.BoostTierId
import com.soukmar.app.data.remote.dto.ListingDto
import com.soukmar.app.data.repository.ApiResult
import com.soukmar.app.data.repository.ListingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BoostListingViewModel @Inject constructor(
    private val listingRepository: ListingRepository
) : ViewModel() {

    private var listingId: String = ""

    var listing by mutableStateOf<ListingDto?>(null)
        private set
    var boostStatus by mutableStateOf<BoostStatusDto?>(null)
        private set
    var loading by mutableStateOf(true)
        private set
    var loadError by mutableStateOf(false)
        private set

    var selectedTiers by mutableStateOf<Set<BoostTierId>>(emptySet())
        private set

    var submitting by mutableStateOf(false)
        private set
    var submitted by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun load(id: String) {
        listingId = id
        viewModelScope.launch {
            loading = true
            loadError = false
            val listingResult = listingRepository.getListing(listingId)
            val statusResult = listingRepository.getBoostStatus(listingId)
            if (listingResult is ApiResult.Success) listing = listingResult.data else loadError = true
            if (statusResult is ApiResult.Success) boostStatus = statusResult.data
            loading = false
        }
    }

    fun toggle(tier: BoostTierId) {
        selectedTiers = if (tier in selectedTiers) selectedTiers - tier else selectedTiers + tier
    }

    /** True while [until] (an ISO date string from [boostStatus]) is still in the future. */
    fun isActiveUntil(until: String?): Boolean {
        if (until == null) return false
        return try {
            java.time.Instant.parse(until).isAfter(java.time.Instant.now())
        } catch (e: Exception) {
            false
        }
    }

    fun submit(onSelectAtLeastOne: () -> Unit) {
        if (selectedTiers.isEmpty()) { onSelectAtLeastOne(); return }
        if (submitting) return
        submitting = true
        errorMessage = null
        viewModelScope.launch {
            when (val result = listingRepository.requestBoost(listingId, selectedTiers.map { it.name })) {
                is ApiResult.Success -> {
                    boostStatus = boostStatus?.copy(pendingRequest = result.data) ?: BoostStatusDto(pendingRequest = result.data)
                    submitted = true
                }
                is ApiResult.Error -> errorMessage = result.message
            }
            submitting = false
        }
    }
}
