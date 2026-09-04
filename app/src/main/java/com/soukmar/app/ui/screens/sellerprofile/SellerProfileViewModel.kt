package com.soukmar.app.ui.screens.sellerprofile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val reviewRepository: ReviewRepository
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

    fun load(sellerId: String) {
        viewModelScope.launch {
            loading = true
            notFound = false
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
}
