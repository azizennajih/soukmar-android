package com.soukmar.app.ui.screens.savedsearches

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soukmar.app.data.remote.dto.SavedSearchDto
import com.soukmar.app.data.repository.ApiResult
import com.soukmar.app.data.repository.SavedSearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedSearchesViewModel @Inject constructor(
    private val savedSearchRepository: SavedSearchRepository
) : ViewModel() {

    var searches by mutableStateOf<List<SavedSearchDto>>(emptyList())
        private set
    var loading by mutableStateOf(true)
        private set

    fun load() {
        viewModelScope.launch {
            loading = true
            when (val result = savedSearchRepository.getAll()) {
                is ApiResult.Success -> searches = result.data
                is ApiResult.Error -> { /* keep whatever list was already shown */ }
            }
            loading = false
        }
    }

    fun remove(id: String) {
        val previous = searches
        searches = searches.filter { it.id != id }
        viewModelScope.launch {
            val result = savedSearchRepository.delete(id)
            if (result is ApiResult.Error) searches = previous
        }
    }
}
