package com.soukmar.app.ui.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soukmar.app.data.local.TokenManager
import com.soukmar.app.data.remote.ApiService
import com.soukmar.app.data.remote.dto.UserDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val api: ApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    var user by mutableStateOf<UserDto?>(null)
    var loading by mutableStateOf(true)

    init {
        viewModelScope.launch {
            loading = true
            try {
                val res = api.me()
                if (res.isSuccessful) user = res.body()
            } catch (_: Exception) { }
            loading = false
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            tokenManager.clear()
            onDone()
        }
    }
}
