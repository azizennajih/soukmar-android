package com.soukmar.app.ui.screens.profil

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soukmar.app.data.remote.dto.IdVerificationStatusDto
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
    var accountType by mutableStateOf("PRIVATE")
    var saving by mutableStateOf(false)
        private set
    var successMessage by mutableStateOf<String?>(null)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    var uploadingImage by mutableStateOf(false)
        private set

    var phoneCodeSent by mutableStateOf(false)
        private set
    var phoneCode by mutableStateOf("")
    var phoneSendingCode by mutableStateOf(false)
        private set
    var phoneVerifying by mutableStateOf(false)
        private set
    var phoneMessage by mutableStateOf<String?>(null)
        private set
    var phoneErrorMessage by mutableStateOf<String?>(null)
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

    // ID verification (free KYC-lite: ID photo + selfie, reviewed manually
    // by an admin) — "NONE" mirrors the web's default when GET returns null.
    var idVerificationStatus by mutableStateOf("NONE")
        private set
    var idVerificationNote by mutableStateOf<String?>(null)
        private set
    var idImageUri by mutableStateOf<Uri?>(null)
    var selfieImageUri by mutableStateOf<Uri?>(null)
    var submittingIdVerification by mutableStateOf(false)
        private set
    var idVerificationMessage by mutableStateOf<String?>(null)
        private set
    var idVerificationErrorMessage by mutableStateOf<String?>(null)
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
                    accountType = result.data.accountType ?: "PRIVATE"
                }
                is ApiResult.Error -> loadError = true
            }
            loading = false
        }
        loadIdVerificationStatus()
    }

    private fun loadIdVerificationStatus() {
        viewModelScope.launch {
            when (val result = authRepository.getIdVerificationStatus()) {
                is ApiResult.Success -> {
                    val v: IdVerificationStatusDto? = result.data
                    idVerificationStatus = v?.status ?: "NONE"
                    idVerificationNote = v?.adminNote
                }
                is ApiResult.Error -> { /* non-essential — leave the form open, mirrors the web */ }
            }
        }
    }

    fun submitIdVerification() {
        val idUri = idImageUri
        val selfieUri = selfieImageUri
        if (idUri == null || selfieUri == null) {
            idVerificationErrorMessage = "Veuillez ajouter les deux photos."
            return
        }
        if (submittingIdVerification) return
        submittingIdVerification = true
        idVerificationMessage = null
        idVerificationErrorMessage = null
        viewModelScope.launch {
            val idUpload = uploadRepository.uploadImages(listOf(idUri), type = "idVerification")
            val selfieUpload = uploadRepository.uploadImages(listOf(selfieUri), type = "idVerification")
            val idUrl = (idUpload as? ApiResult.Success)?.data?.firstOrNull()
            val selfieUrl = (selfieUpload as? ApiResult.Success)?.data?.firstOrNull()
            if (idUrl == null || selfieUrl == null) {
                idVerificationErrorMessage = "L'envoi a échoué. Réessayez."
                submittingIdVerification = false
                return@launch
            }
            when (val result = authRepository.submitIdVerification(idUrl, selfieUrl)) {
                is ApiResult.Success -> {
                    idVerificationStatus = result.data.status
                    idImageUri = null
                    selfieImageUri = null
                    idVerificationMessage = "Votre demande a été envoyée. Nous l'examinerons sous peu."
                }
                is ApiResult.Error -> idVerificationErrorMessage = result.message
            }
            submittingIdVerification = false
        }
    }

    fun saveProfile() {
        if (name.isBlank()) { errorMessage = "Le nom est requis."; return }
        if (saving) return
        saving = true
        successMessage = null
        errorMessage = null
        viewModelScope.launch {
            when (val result = authRepository.updateProfile(name.trim(), phone.trim().ifBlank { null }, city.trim().ifBlank { null }, accountType)) {
                is ApiResult.Success -> {
                    profile = result.data
                    successMessage = "Profil mis à jour."
                    // A changed phone number invalidates any prior verification
                    // server-side — drop any in-progress code entry for the old number.
                    phoneCodeSent = false
                    phoneCode = ""
                    phoneMessage = null
                    phoneErrorMessage = null
                }
                is ApiResult.Error -> errorMessage = result.message
            }
            saving = false
        }
    }

    fun sendPhoneCode() {
        if (phoneSendingCode) return
        phoneMessage = null
        phoneErrorMessage = null
        phoneSendingCode = true
        viewModelScope.launch {
            when (val result = authRepository.sendPhoneCode()) {
                is ApiResult.Success -> {
                    phoneCodeSent = true
                    phoneCode = ""
                    phoneMessage = "Code envoyé par SMS."
                }
                is ApiResult.Error -> phoneErrorMessage = result.message
            }
            phoneSendingCode = false
        }
    }

    fun verifyPhoneCode() {
        if (phoneCode.isBlank() || phoneVerifying) return
        phoneMessage = null
        phoneErrorMessage = null
        phoneVerifying = true
        viewModelScope.launch {
            when (val result = authRepository.verifyPhoneCode(phoneCode.trim())) {
                is ApiResult.Success -> {
                    profile = profile?.copy(phoneVerified = true)
                    phoneCodeSent = false
                    phoneCode = ""
                    phoneMessage = "Numéro de téléphone vérifié avec succès !"
                }
                is ApiResult.Error -> phoneErrorMessage = result.message
            }
            phoneVerifying = false
        }
    }

    fun pickAvatar(uri: Uri) {
        if (uploadingImage) return
        uploadingImage = true
        errorMessage = null
        viewModelScope.launch {
            when (val uploadResult = uploadRepository.uploadImages(listOf(uri), type = "avatar")) {
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
