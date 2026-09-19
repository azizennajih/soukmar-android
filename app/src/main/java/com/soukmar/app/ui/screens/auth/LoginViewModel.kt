package com.soukmar.app.ui.screens.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soukmar.app.data.country.CountryRepository
import com.soukmar.app.data.repository.ApiResult
import com.soukmar.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val countryRepository: CountryRepository
) : ViewModel() {

    // Lets a not-yet-logged-in visitor pick their country before signing in
    // — mirrors the web navbar's country switcher being visible everywhere,
    // including on auth pages, the same way LanguageSwitcher already is here.
    var country by mutableStateOf(countryRepository.country)
        private set

    fun selectCountry(code: String) {
        if (code == country) return
        country = code
        countryRepository.updateCountry(code)
    }

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var loading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var unverifiedEmail by mutableStateOf<String?>(null)
    var resendLoading by mutableStateOf(false)
    var resendOk by mutableStateOf(false)

    fun submit(onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            error = "Veuillez remplir tous les champs."
            return
        }
        loading = true
        error = null
        unverifiedEmail = null
        viewModelScope.launch {
            when (val result = authRepository.login(email.trim(), password)) {
                is ApiResult.Success -> {
                    loading = false
                    onSuccess()
                }
                is ApiResult.Error -> {
                    loading = false
                    if (result.unverified) unverifiedEmail = email.trim()
                    error = result.message
                }
            }
        }
    }

    fun resendVerification() {
        val target = unverifiedEmail ?: return
        if (resendLoading) return
        resendLoading = true
        resendOk = false
        viewModelScope.launch {
            val result = authRepository.resendVerification(target)
            resendLoading = false
            resendOk = result is ApiResult.Success
            if (result is ApiResult.Error) error = result.message
        }
    }
}
