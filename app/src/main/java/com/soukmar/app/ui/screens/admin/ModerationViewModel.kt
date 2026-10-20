package com.soukmar.app.ui.screens.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soukmar.app.data.remote.dto.ModerationDetailDto
import com.soukmar.app.data.remote.dto.ModerationQueueItemDto
import com.soukmar.app.data.remote.dto.ModerationSummaryDto
import com.soukmar.app.data.repository.ApiResult
import com.soukmar.app.data.repository.DecisionResult
import com.soukmar.app.data.repository.ModerationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

const val REASON_MIN = 5
const val REASON_MAX = 1000

/** Review area for listings. Mirrors the web dashboard (admin/moderation component): the server enforces every rule,
 * this only drives the screens. */
@HiltViewModel
class ModerationViewModel @Inject constructor(
    private val repository: ModerationRepository
) : ViewModel() {

    var summary by mutableStateOf<ModerationSummaryDto?>(null)
        private set

    // ── queue ──
    var items by mutableStateOf<List<ModerationQueueItemDto>>(emptyList())
        private set
    var total by mutableStateOf(0)
        private set
    var page by mutableStateOf(1)
        private set
    var pages by mutableStateOf(1)
        private set
    var loading by mutableStateOf(true)
        private set
    var loadingMore by mutableStateOf(false)
        private set
    var loadError by mutableStateOf(false)
        private set

    var status by mutableStateOf("PENDING")
        private set
    var country by mutableStateOf("")
        private set
    var category by mutableStateOf("")
        private set
    var sort by mutableStateOf("oldest")
        private set
    var query by mutableStateOf("")

    private var started = false

    fun start() {
        if (started) return
        started = true
        loadSummary()
        loadQueue()
    }

    fun loadSummary() {
        viewModelScope.launch {
            when (val r = repository.summary()) {
                is ApiResult.Success -> summary = r.data
                is ApiResult.Error -> { /* counters just stay as they were */ }
            }
        }
    }

    fun loadQueue() {
        viewModelScope.launch {
            loading = true
            loadError = false
            when (val r = repository.queue(status, country, category, query, sort, 1)) {
                is ApiResult.Success -> {
                    items = r.data.listings
                    total = r.data.total
                    page = r.data.page
                    pages = r.data.pages
                }
                is ApiResult.Error -> { items = emptyList(); loadError = true }
            }
            loading = false
        }
    }

    fun loadMore() {
        if (loadingMore || loading || page >= pages) return
        viewModelScope.launch {
            loadingMore = true
            when (val r = repository.queue(status, country, category, query, sort, page + 1)) {
                is ApiResult.Success -> {
                    items = items + r.data.listings.filter { n -> items.none { it.id == n.id } }
                    page = r.data.page
                    pages = r.data.pages
                }
                is ApiResult.Error -> { /* the button stays, the admin can retry */ }
            }
            loadingMore = false
        }
    }

    fun selectStatus(s: String) {
        status = s
        // Waiting listings are worked oldest first; every other view reads newest first.
        sort = if (s == "PENDING") "oldest" else "newest"
        loadQueue()
    }

    fun selectCountry(c: String) { country = c; loadQueue() }
    fun selectCategory(c: String) { category = c; loadQueue() }
    fun selectSort(s: String) { sort = s; loadQueue() }
    fun search() = loadQueue()

    fun resetFilters() {
        status = "PENDING"; country = ""; category = ""; query = ""; sort = "oldest"
        loadQueue()
    }

    // ── detail ──
    var detail by mutableStateOf<ModerationDetailDto?>(null)
        private set
    var detailLoading by mutableStateOf(false)
        private set

    /** Key of a translated message to show once (snackbar). */
    var messageKey by mutableStateOf<String?>(null)
        private set
    var extraMessageKey by mutableStateOf<String?>(null)
        private set

    fun clearMessage() { messageKey = null; extraMessageKey = null }

    fun openDetail(id: String) {
        viewModelScope.launch {
            detailLoading = true
            cancelDecision()
            when (val r = repository.detail(id)) {
                is ApiResult.Success -> detail = r.data
                is ApiResult.Error -> messageKey = "auth.generic_error"
            }
            detailLoading = false
        }
    }

    fun closeDetail() {
        detail = null
        cancelDecision()
        loadSummary()
        loadQueue()
    }

    // ── decision ──
    /** URL form of the action that is waiting for a reason: reject | request-changes | suspend. */
    var reasonAction by mutableStateOf<String?>(null)
        private set
    var reason by mutableStateOf("")
    var allowResubmit by mutableStateOf(false)
    var submitting by mutableStateOf(false)
        private set
    /** approve / requeue wait for a yes/no confirmation. */
    var confirmAction by mutableStateOf<String?>(null)
        private set

    val reasonValid: Boolean get() = reason.trim().length in REASON_MIN..REASON_MAX

    fun startReason(action: String) { reasonAction = action; reason = ""; allowResubmit = false }
    fun askConfirm(action: String) { confirmAction = action }
    fun cancelDecision() { reasonAction = null; confirmAction = null; reason = ""; allowResubmit = false }

    fun submitReason() {
        val action = reasonAction ?: return
        if (!reasonValid) return
        decide(action, reason.trim(), if (action == "reject") allowResubmit else null)
    }

    fun confirm() {
        val action = confirmAction ?: return
        decide(action, null, null)
    }

    private fun decide(action: String, reasonText: String?, resubmit: Boolean?) {
        val d = detail ?: return
        if (submitting) return
        viewModelScope.launch {
            submitting = true
            when (val r = repository.decide(d.listing.id, action, reasonText, resubmit, d.listing.status)) {
                is DecisionResult.Done -> {
                    messageKey = "moderation.done_" + action.replace('-', '_')
                    if (r.emailStatus == "FAILED") extraMessageKey = "moderation.mail_failed_note"
                    cancelDecision()
                    refreshAfterDecision(d.listing.id)
                }
                DecisionResult.Conflict -> {
                    messageKey = "moderation.conflict"
                    cancelDecision()
                    refreshAfterDecision(d.listing.id)
                }
                DecisionResult.ReasonInvalid -> messageKey = "moderation.reason_required"
                is DecisionResult.Failed -> messageKey = "auth.generic_error"
            }
            submitting = false
        }
    }

    private suspend fun refreshAfterDecision(id: String) {
        when (val r = repository.detail(id)) {
            is ApiResult.Success -> detail = r.data
            is ApiResult.Error -> { /* keep the old view */ }
        }
        loadSummary()
    }

    var resendingEventId by mutableStateOf<String?>(null)
        private set

    fun resendMail(eventId: String) {
        val d = detail ?: return
        if (resendingEventId != null) return
        viewModelScope.launch {
            resendingEventId = eventId
            when (val r = repository.resendMail(eventId)) {
                is ApiResult.Success -> messageKey = if (r.data) "moderation.mail_resent_ok" else "moderation.mail_resent_fail"
                is ApiResult.Error -> messageKey = "moderation.mail_not_retryable"
            }
            resendingEventId = null
            refreshAfterDecision(d.listing.id)
        }
    }
}
