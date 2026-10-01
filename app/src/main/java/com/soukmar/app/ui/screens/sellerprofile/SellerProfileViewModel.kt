package com.soukmar.app.ui.screens.sellerprofile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soukmar.app.data.local.TokenManager
import com.soukmar.app.data.remote.dto.ListingDto
import com.soukmar.app.data.remote.dto.ReviewWithDetailsDto
import com.soukmar.app.data.remote.dto.SellerProfileDto
import com.soukmar.app.data.repository.ApiResult
import com.soukmar.app.data.repository.ReviewRepository
import com.soukmar.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SellerProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val reviewRepository: ReviewRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    var loading by mutableStateOf(true)
        private set
    var notFound by mutableStateOf(false)
        private set

    var profile by mutableStateOf<SellerProfileDto?>(null)
        private set
    var listings by mutableStateOf<List<ListingDto>>(emptyList())
        private set
    var reviews by mutableStateOf<List<ReviewWithDetailsDto>>(emptyList())
        private set

    /** Whether the viewer is logged in / is viewing their own profile — the
     * follow button is hidden for both an own profile and a logged-out
     * visitor (who sees a login link instead), mirrors the web's
     * `auth.currentUser()?.id !== profile.id`/`auth.isLoggedIn` gates. */
    var isLoggedIn by mutableStateOf(false)
        private set
    var isOwnProfile by mutableStateOf(false)
        private set
    var followSubmitting by mutableStateOf(false)
        private set

    fun load(sellerId: String) {
        viewModelScope.launch {
            loading = true
            notFound = false
            isLoggedIn = tokenManager.isLoggedIn()
            isOwnProfile = isLoggedIn && tokenManager.currentUserId() == sellerId
            when (val result = userRepository.getSellerProfile(sellerId)) {
                is ApiResult.Success -> profile = result.data
                is ApiResult.Error -> notFound = true
            }
            if (!notFound) {
                launch {
                    when (val result = userRepository.getSellerListings(sellerId)) {
                        is ApiResult.Success -> listings = result.data
                        is ApiResult.Error -> { /* listings grid just stays empty */ }
                    }
                }
                launch {
                    when (val result = reviewRepository.getForUser(sellerId)) {
                        is ApiResult.Success -> reviews = result.data.reviews
                        is ApiResult.Error -> { /* reviews list just stays empty */ }
                    }
                }
            }
            loading = false
        }
    }

    fun toggleFollow() {
        val current = profile ?: return
        if (followSubmitting) return
        followSubmitting = true
        viewModelScope.launch {
            val result = if (current.isFollowing) userRepository.unfollowUser(current.id) else userRepository.followUser(current.id)
            when (result) {
                is ApiResult.Success -> profile = current.copy(isFollowing = result.data.following, followerCount = result.data.followerCount)
                is ApiResult.Error -> { /* leave state unchanged, silent like other non-essential actions here */ }
            }
            followSubmitting = false
        }
    }
}
