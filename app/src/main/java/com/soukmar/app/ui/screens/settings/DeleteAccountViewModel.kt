package com.soukmar.app.ui.screens.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soukmar.app.data.repository.ApiResult
import com.soukmar.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeleteAccountViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    var password by mutableStateOf("")
    var confirmed by mutableStateOf(false)
    var submitting by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var deleted by mutableStateOf(false)
        private set

    val canSubmit: Boolean
        get() = password.isNotBlank() && confirmed && !submitting

    fun submit(onLoggedOut: () -> Unit) {
        if (!canSubmit) return
        submitting = true
        errorMessage = null
        viewModelScope.launch {
            when (val result = authRepository.deleteAccount(password)) {
                is ApiResult.Success -> {
                    deleted = true
                    authRepository.logout()
                    delay(1200)
                    onLoggedOut()
                }
                is ApiResult.Error -> {
                    submitting = false
                    errorMessage = result.message
                }
            }
        }
    }
}
