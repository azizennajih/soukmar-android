package com.soukmar.app.ui.screens.listingdetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soukmar.app.data.local.TokenManager
import com.soukmar.app.data.remote.dto.ListingDto
import com.soukmar.app.data.repository.ApiResult
import com.soukmar.app.data.repository.ChatRepository
import com.soukmar.app.data.repository.ListingRepository
import com.soukmar.app.data.repository.ReportRepository
import com.soukmar.app.data.repository.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListingDetailViewModel @Inject constructor(
    private val listingRepository: ListingRepository,
    private val reviewRepository: ReviewRepository,
    private val reportRepository: ReportRepository,
    private val chatRepository: ChatRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    var listing by mutableStateOf<ListingDto?>(null)
        private set
    var loading by mutableStateOf(true)
        private set
    var loadError by mutableStateOf(false)
        private set

    var isLoggedIn by mutableStateOf(false)
        private set
    var currentUserId by mutableStateOf<String?>(null)
        private set

    var favorited by mutableStateOf(false)
        private set
    var favLoading by mutableStateOf(false)
        private set

    var canReview by mutableStateOf(false)
        private set
    private var revieweeId: String? = null
    var showReviewForm by mutableStateOf(false)
    var reviewRating by mutableStateOf(5)
    var reviewComment by mutableStateOf("")
    var reviewSubmitting by mutableStateOf(false)
        private set
    var reviewSubmitted by mutableStateOf(false)
        private set

    var reportOpen by mutableStateOf(false)
    var reportReason by mutableStateOf("")
    var reportSubmitting by mutableStateOf(false)
        private set
    var reportSubmitted by mutableStateOf(false)
        private set
    var reportError by mutableStateOf<String?>(null)
        private set

    var chatStarting by mutableStateOf(false)
        private set
    var chatMessage by mutableStateOf<String?>(null)
        private set
    var navigateToChatId by mutableStateOf<String?>(null)
        private set

    fun load(id: String) {
        viewModelScope.launch {
            loading = true
            loadError = false
            isLoggedIn = tokenManager.isLoggedIn()
            currentUserId = tokenManager.currentUserId()
            when (val result = listingRepository.getListing(id)) {
                is ApiResult.Success -> {
                    listing = result.data
                    if (isLoggedIn) {
                        favorited = listingRepository.getFavoriteIds().contains(id)
                        checkCanReview(id)
                    }
                }
                is ApiResult.Error -> loadError = true
            }
            loading = false
        }
    }

    private fun checkCanReview(listingId: String) {
        viewModelScope.launch {
            when (val result = reviewRepository.canReview(listingId)) {
                is ApiResult.Success -> {
                    canReview = result.data.canReview
                    revieweeId = result.data.revieweeId
                }
                is ApiResult.Error -> { /* review prompt is optional, silently skip */ }
            }
        }
    }

    val priceComparisonPct: Int?
        get() {
            val price = listing?.price ?: return null
            val avg = listing?.avgPrice ?: return null
            if (avg == 0.0) return null
            return Math.round(((price - avg) / avg) * 100).toInt()
        }

    fun toggleFavorite() {
        val id = listing?.id ?: return
        if (favLoading) return
        val wasFav = favorited
        favorited = !wasFav
        favLoading = true
        viewModelScope.launch {
            val ok = if (wasFav) listingRepository.removeFavorite(id) else listingRepository.addFavorite(id)
            if (!ok) favorited = wasFav
            favLoading = false
        }
    }

    fun submitReview() {
        val id = listing?.id ?: return
        val reviewee = revieweeId ?: return
        if (reviewSubmitting) return
        reviewSubmitting = true
        viewModelScope.launch {
            when (reviewRepository.submitReview(id, reviewee, reviewRating, reviewComment)) {
                is ApiResult.Success -> {
                    reviewSubmitted = true
                    showReviewForm = false
                    canReview = false
                }
                is ApiResult.Error -> { /* leave the form open so the user can retry */ }
            }
            reviewSubmitting = false
        }
    }

    fun submitReport() {
        val reportedId = listing?.userId ?: return
        if (reportReason.trim().length < 10) {
            reportError = "Merci de décrire la raison en au moins 10 caractères."
            return
        }
        reportSubmitting = true
        reportError = null
        viewModelScope.launch {
            when (val result = reportRepository.submit(reportedId, listing?.id, reportReason.trim())) {
                is ApiResult.Success -> {
                    reportSubmitting = false
                    reportSubmitted = true
                    reportOpen = false
                }
                is ApiResult.Error -> {
                    reportSubmitting = false
                    reportError = result.message
                }
            }
        }
    }

    fun cancelReport() {
        reportOpen = false
        reportError = null
    }

    fun startChat() {
        val id = listing?.id ?: return
        if (chatStarting) return
        chatStarting = true
        chatMessage = null
        viewModelScope.launch {
            when (val result = chatRepository.startConversation(id)) {
                is ApiResult.Success -> navigateToChatId = result.data.id
                is ApiResult.Error -> chatMessage = result.message
            }
            chatStarting = false
        }
    }

    fun clearChatMessage() { chatMessage = null }
    fun clearNavigateToChatId() { navigateToChatId = null }
}
