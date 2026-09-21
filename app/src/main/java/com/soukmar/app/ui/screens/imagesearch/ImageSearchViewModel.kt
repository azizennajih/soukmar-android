package com.soukmar.app.ui.screens.imagesearch

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soukmar.app.data.country.CountryRepository
import com.soukmar.app.data.remote.dto.ListingDto
import com.soukmar.app.data.repository.ApiResult
import com.soukmar.app.data.repository.UploadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/** "Search by photo" — mirrors the web's `ImageSearchComponent`: pick/take
 * one photo, upload it to the dedicated public `POST /listings/search-by-
 * image` endpoint (not the general `/upload`), show ranked results (closest
 * visual match first) or an empty state. Free — the backend hashes the
 * photo locally (sharp dHash), no paid vision API. */
@HiltViewModel
class ImageSearchViewModel @Inject constructor(
    private val uploadRepository: UploadRepository,
    private val countryRepository: CountryRepository
) : ViewModel() {

    var previewUri by mutableStateOf<Uri?>(null)
        private set
    var results by mutableStateOf<List<ListingDto>>(emptyList())
        private set
    var loading by mutableStateOf(false)
        private set
    var searched by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun runSearch(uri: Uri) {
        previewUri = uri
        loading = true
        searched = true
        errorMessage = null
        results = emptyList()
        viewModelScope.launch {
            when (val result = uploadRepository.searchByImage(uri, countryRepository.country)) {
                is ApiResult.Success -> results = result.data
                is ApiResult.Error -> errorMessage = "La recherche a échoué. Veuillez réessayer."
            }
            loading = false
        }
    }
}
