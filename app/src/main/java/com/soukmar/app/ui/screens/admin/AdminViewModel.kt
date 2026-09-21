package com.soukmar.app.ui.screens.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soukmar.app.data.remote.dto.AdminIdVerificationDto
import com.soukmar.app.data.remote.dto.AdminReportDto
import com.soukmar.app.data.repository.AdminRepository
import com.soukmar.app.data.repository.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AdminTab { REPORTS, ID_VERIFICATIONS }

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    var currentTab by mutableStateOf(AdminTab.REPORTS)

    var reports by mutableStateOf<List<AdminReportDto>>(emptyList())
        private set
    var loading by mutableStateOf(true)
        private set
    var loadError by mutableStateOf(false)
        private set

    // Mirrors the web admin page's default filter — the moderation queue
    // opens on the actionable subset, not everything.
    var filter by mutableStateOf("PENDING")

    var actionTarget by mutableStateOf<AdminReportDto?>(null)
        private set
    var actionStatus by mutableStateOf<String?>(null)
        private set
    var actionNote by mutableStateOf("")
    var actionSubmitting by mutableStateOf(false)
        private set

    val filteredReports: List<AdminReportDto>
        get() = if (filter == "ALL") reports else reports.filter { it.status == filter }

    fun countFor(status: String): Int =
        if (status == "ALL") reports.size else reports.count { it.status == status }

    // ID verifications (free KYC-lite queue)
    var idVerifications by mutableStateOf<List<AdminIdVerificationDto>>(emptyList())
        private set
    var idVerificationsLoading by mutableStateOf(true)
        private set
    var idVerificationsLoadError by mutableStateOf(false)
        private set
    var idVerificationFilter by mutableStateOf("PENDING")

    var idActionTarget by mutableStateOf<AdminIdVerificationDto?>(null)
        private set
    var idActionStatus by mutableStateOf<String?>(null)
        private set
    var idActionNote by mutableStateOf("")
    var idActionSubmitting by mutableStateOf(false)
        private set

    val filteredIdVerifications: List<AdminIdVerificationDto>
        get() = if (idVerificationFilter == "ALL") idVerifications else idVerifications.filter { it.status == idVerificationFilter }

    fun idCountFor(status: String): Int =
        if (status == "ALL") idVerifications.size else idVerifications.count { it.status == status }

    fun load() {
        viewModelScope.launch {
            loading = true
            loadError = false
            when (val result = adminRepository.getReports()) {
                is ApiResult.Success -> reports = result.data
                is ApiResult.Error -> loadError = true
            }
            loading = false
        }
        viewModelScope.launch {
            idVerificationsLoading = true
            idVerificationsLoadError = false
            when (val result = adminRepository.getIdVerifications()) {
                is ApiResult.Success -> idVerifications = result.data
                is ApiResult.Error -> idVerificationsLoadError = true
            }
            idVerificationsLoading = false
        }
    }

    fun openIdAction(v: AdminIdVerificationDto, status: String) {
        idActionTarget = v
        idActionStatus = status
        idActionNote = ""
    }

    fun cancelIdAction() {
        idActionTarget = null
        idActionStatus = null
        idActionNote = ""
    }

    fun confirmIdAction() {
        val v = idActionTarget ?: return
        val status = idActionStatus ?: return
        if (idActionSubmitting) return
        idActionSubmitting = true
        viewModelScope.launch {
            when (val result = adminRepository.reviewIdVerification(v.id, status, idActionNote)) {
                is ApiResult.Success -> {
                    val updated = result.data
                    idVerifications = idVerifications.map {
                        if (it.id == v.id) it.copy(status = updated.status, adminNote = updated.adminNote, reviewedAt = updated.reviewedAt) else it
                    }
                    idActionTarget = null
                    idActionStatus = null
                    idActionNote = ""
                }
                is ApiResult.Error -> { /* leave the dialog open so the admin can retry */ }
            }
            idActionSubmitting = false
        }
    }

    fun openAction(report: AdminReportDto, status: String) {
        actionTarget = report
        actionStatus = status
        actionNote = ""
    }

    fun cancelAction() {
        actionTarget = null
        actionStatus = null
        actionNote = ""
    }

    fun confirmAction() {
        val report = actionTarget ?: return
        val status = actionStatus ?: return
        if (actionSubmitting) return
        actionSubmitting = true
        viewModelScope.launch {
            when (val result = adminRepository.updateReport(report.id, status, actionNote)) {
                is ApiResult.Success -> {
                    // The PATCH response has no reporter/reported/listing
                    // includes (only GET /reports/admin does) — merge just
                    // the changed fields into the already-loaded row instead
                    // of replacing it wholesale, or those refs go blank.
                    val updated = result.data
                    reports = reports.map {
                        if (it.id == report.id) {
                            it.copy(status = updated.status, adminNote = updated.adminNote, resolvedAt = updated.resolvedAt)
                        } else it
                    }
                    actionTarget = null
                    actionStatus = null
                    actionNote = ""
                }
                is ApiResult.Error -> { /* leave the dialog open so the admin can retry */ }
            }
            actionSubmitting = false
        }
    }
}
