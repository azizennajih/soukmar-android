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
class ResetPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    var loading by mutableStateOf(false)
    var success by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    fun submit(token: String) {
        if (password.length < 6) { error = "Le mot de passe doit contenir au moins 6 caractères."; return }
        if (password != confirmPassword) { error = "Les mots de passe ne correspondent pas."; return }
        loading = true
        error = null
        viewModelScope.launch {
            val result = authRepository.resetPassword(token, password)
            loading = false
            when (result) {
                is ApiResult.Success -> success = true
                is ApiResult.Error -> error = result.message
            }
        }
    }
}
