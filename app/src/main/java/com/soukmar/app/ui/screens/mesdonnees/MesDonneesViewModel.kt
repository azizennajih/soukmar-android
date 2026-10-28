package com.soukmar.app.ui.screens.mesdonnees

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soukmar.app.data.i18n.appText
import com.soukmar.app.data.remote.dto.BlockedUserDto
import com.soukmar.app.data.remote.dto.EmailPreferencesDto
import com.soukmar.app.data.repository.ApiResult
import com.soukmar.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Personal-data self-service (web: /mes-donnees): e-mail preferences, blocked users, data download. */
@HiltViewModel
class MesDonneesViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    var prefs by mutableStateOf<EmailPreferencesDto?>(null)
    var blocked by mutableStateOf<List<BlockedUserDto>?>(null)
    var exporting by mutableStateOf(false)
    var message by mutableStateOf<String?>(null)

    /** The prepared export, written out once the user has picked where to save it. */
    var exportBytes: ByteArray? = null
        private set

    fun load() {
        viewModelScope.launch {
            prefs = (userRepository.getEmailPreferences() as? ApiResult.Success)?.data
            blocked = (userRepository.getBlockedUsers() as? ApiResult.Success)?.data ?: emptyList()
        }
    }

    /** Saves right away; the switch goes back if the server refuses. */
    fun setPref(update: (EmailPreferencesDto) -> EmailPreferencesDto) {
        val current = prefs ?: return
        val next = update(current)
        prefs = next
        viewModelScope.launch {
            when (val r = userRepository.setEmailPreferences(next)) {
                is ApiResult.Success -> { prefs = r.data; message = appText("mes_donnees.saved", "OK") }
                is ApiResult.Error -> { prefs = current; message = r.message }
            }
        }
    }

    fun unblock(user: BlockedUserDto) {
        viewModelScope.launch {
            when (val r = userRepository.unblockUser(user.id)) {
                is ApiResult.Success -> { blocked = blocked?.filter { it.id != user.id }; message = appText("mes_donnees.saved", "OK") }
                is ApiResult.Error -> message = r.message
            }
        }
    }

    /** Fetches the export; [onReady] runs once the bytes are available (then the save dialog opens). */
    fun prepareExport(onReady: () -> Unit) {
        exporting = true
        viewModelScope.launch {
            when (val r = userRepository.exportMyData()) {
                is ApiResult.Success -> { exportBytes = r.data; onReady() }
                is ApiResult.Error -> message = appText("mes_donnees.export_error", r.message)
            }
            exporting = false
        }
    }

    fun exportSaved() { exportBytes = null }
    fun exportFailed() { message = appText("mes_donnees.export_error", "") }
    fun clearMessage() { message = null }
}
