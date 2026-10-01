package com.soukmar.app.ui.screens.mesabonnements

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soukmar.app.data.remote.dto.FollowedUserDto
import com.soukmar.app.data.repository.ApiResult
import com.soukmar.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Mirrors the web's MesAbonnementsComponent — list of followed sellers/
 * buyers, each row lets you unfollow directly without leaving the screen. */
@HiltViewModel
class MesAbonnementsViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    var users by mutableStateOf<List<FollowedUserDto>>(emptyList())
        private set
    var loading by mutableStateOf(true)
        private set

    /** IDs with an in-flight unfollow request — drives each row's own
     * FollowButton disabled state without a separate per-row ViewModel. */
    var submittingIds by mutableStateOf<Set<String>>(emptySet())
        private set

    fun load() {
        viewModelScope.launch {
            loading = true
            when (val result = userRepository.getFollowing()) {
                is ApiResult.Success -> users = result.data
                is ApiResult.Error -> { /* empty list is a fine fallback here */ }
            }
            loading = false
        }
    }

    /** Mirrors the web's onUnfollow(): the row is only removed once the
     * backend confirms the unfollow, not optimistically beforehand. */
    fun unfollow(userId: String) {
        if (userId in submittingIds) return
        submittingIds = submittingIds + userId
        viewModelScope.launch {
            when (userRepository.unfollowUser(userId)) {
                is ApiResult.Success -> users = users.filter { it.id != userId }
                is ApiResult.Error -> { /* leave the row in place, user can retry */ }
            }
            submittingIds = submittingIds - userId
        }
    }
}
