package com.soukmar.app.ui.screens.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soukmar.app.data.repository.ApiResult
import com.soukmar.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    var email by mutableStateOf("")
    var loading by mutableStateOf(false)
    var sent by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    fun submit() {
        if (email.isBlank()) return
        loading = true
        error = null
        viewModelScope.launch {
            val result = authRepository.forgotPassword(email.trim())
            loading = false
            when (result) {
                is ApiResult.Success -> sent = true
                is ApiResult.Error -> error = result.message
            }
        }
    }
}
