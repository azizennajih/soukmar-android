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
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    var name by mutableStateOf("")
    var email by mutableStateOf("")
    var phone by mutableStateOf("")
    var city by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    var loading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var registeredEmail by mutableStateOf<String?>(null)
    var emailSent by mutableStateOf(false)
    var resendLoading by mutableStateOf(false)
    var resendOk by mutableStateOf(false)

    fun submit() {
        if (password != confirmPassword) { error = "Les mots de passe ne correspondent pas."; return }
        if (password.length < 6) { error = "Le mot de passe doit contenir au moins 6 caractères."; return }
        if (name.isBlank() || email.isBlank()) { error = "Champs requis manquants."; return }

        loading = true
        error = null
        viewModelScope.launch {
            val result = authRepository.register(name.trim(), email.trim(), password, phone.ifBlank { null }, city.ifBlank { null })
            loading = false
            when (result) {
                is ApiResult.Success -> {
                    registeredEmail = email.trim()
                    emailSent = true
                }
                is ApiResult.Error -> error = result.message
            }
        }
    }

    fun resend() {
        val target = registeredEmail ?: return
        if (resendLoading) return
        resendLoading = true
        resendOk = false
        viewModelScope.launch {
            val result = authRepository.resendVerification(target)
            resendLoading = false
            resendOk = result is ApiResult.Success
        }
    }
}
