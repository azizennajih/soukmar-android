package com.soukmar.app.ui.screens.profil

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soukmar.app.data.remote.dto.UserDto
import com.soukmar.app.data.repository.ApiResult
import com.soukmar.app.data.repository.AuthRepository
import com.soukmar.app.data.repository.UploadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfilViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val uploadRepository: UploadRepository
) : ViewModel() {

    var profile by mutableStateOf<UserDto?>(null)
        private set
    var loading by mutableStateOf(true)
        private set
    var loadError by mutableStateOf(false)
        private set

    var name by mutableStateOf("")
    var phone by mutableStateOf("")
    var city by mutableStateOf("")
    var saving by mutableStateOf(false)
        private set
    var successMessage by mutableStateOf<String?>(null)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    var uploadingImage by mutableStateOf(false)
        private set

    var currentPassword by mutableStateOf("")
    var newPassword by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    var pwSaving by mutableStateOf(false)
        private set
    var pwSuccessMessage by mutableStateOf<String?>(null)
        private set
    var pwErrorMessage by mutableStateOf<String?>(null)
        private set

    fun load() {
        viewModelScope.launch {
            loading = true
            loadError = false
            when (val result = authRepository.getMe()) {
                is ApiResult.Success -> {
                    profile = result.data
                    name = result.data.name
                    phone = result.data.phone ?: ""
                    city = result.data.city ?: ""
                }
                is ApiResult.Error -> loadError = true
            }
            loading = false
        }
    }

    fun saveProfile() {
        if (name.isBlank()) { errorMessage = "Le nom est requis."; return }
        if (saving) return
        saving = true
        successMessage = null
        errorMessage = null
        viewModelScope.launch {
            when (val result = authRepository.updateProfile(name.trim(), phone.trim().ifBlank { null }, city.trim().ifBlank { null })) {
                is ApiResult.Success -> {
                    profile = result.data
                    successMessage = "Profil mis à jour."
                }
                is ApiResult.Error -> errorMessage = result.message
            }
            saving = false
        }
    }

    fun pickAvatar(uri: Uri) {
        if (uploadingImage) return
        uploadingImage = true
        errorMessage = null
        viewModelScope.launch {
            when (val uploadResult = uploadRepository.uploadImages(listOf(uri))) {
                is ApiResult.Success -> {
                    val url = uploadResult.data.firstOrNull()
                    if (url != null) {
                        when (val result = authRepository.updateProfileImage(url)) {
                            is ApiResult.Success -> {
                                profile = result.data
                                successMessage = "Photo de profil mise à jour."
                            }
                            is ApiResult.Error -> errorMessage = result.message
                        }
                    }
                }
                is ApiResult.Error -> errorMessage = "Erreur lors du téléchargement de la photo."
            }
            uploadingImage = false
        }
    }

    fun changePassword() {
        pwSuccessMessage = null
        pwErrorMessage = null
        if (newPassword.length < 6) { pwErrorMessage = "Le mot de passe doit contenir au moins 6 caractères."; return }
        if (newPassword != confirmPassword) { pwErrorMessage = "Les mots de passe ne correspondent pas."; return }
        if (pwSaving) return
        pwSaving = true
        viewModelScope.launch {
            when (val result = authRepository.changePassword(currentPassword, newPassword)) {
                is ApiResult.Success -> {
                    pwSuccessMessage = "Mot de passe modifié avec succès."
                    currentPassword = ""
                    newPassword = ""
                    confirmPassword = ""
                }
                is ApiResult.Error -> pwErrorMessage = result.message
            }
            pwSaving = false
        }
    }
}
