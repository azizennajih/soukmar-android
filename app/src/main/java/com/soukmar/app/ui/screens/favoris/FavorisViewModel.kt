package com.soukmar.app.ui.screens.favoris

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soukmar.app.data.remote.dto.ListingDto
import com.soukmar.app.data.repository.ApiResult
import com.soukmar.app.data.repository.ListingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavorisViewModel @Inject constructor(
    private val listingRepository: ListingRepository,
    private val favoritesStore: com.soukmar.app.data.local.FavoritesStore
) : ViewModel() {

    var listings by mutableStateOf<List<ListingDto>>(emptyList())
        private set
    var loading by mutableStateOf(true)
        private set

    fun load() {
        viewModelScope.launch {
            loading = true
            when (val result = listingRepository.getFavorites()) {
                is ApiResult.Success -> { listings = result.data; result.data.forEach { favoritesStore.set(it.id, true) } }
                is ApiResult.Error -> { /* empty list is a fine fallback here */ }
            }
            loading = false
        }
    }

    /** Optimistic removal, mirroring the heart-toggle pattern already used
     * on ListingDetailScreen — revert if the backend call fails. */
    fun removeFavorite(id: String) {
        val previous = listings
        listings = listings.filter { it.id != id }
        viewModelScope.launch {
            val ok = listingRepository.removeFavorite(id)
            if (!ok) listings = previous else favoritesStore.set(id, false)
        }
    }
}
