package com.soukmar.app.ui.screens.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soukmar.app.data.remote.dto.AdminReportDto
import com.soukmar.app.data.repository.AdminRepository
import com.soukmar.app.data.repository.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

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
