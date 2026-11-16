package com.soukmar.app.data.local

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.soukmar.app.data.repository.ListingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/** The ids of the user's favourites, shared by every listing card so the heart shows the same state on all screens.
 * Pure UI state on top of the existing favourites endpoints (GET/POST/DELETE favorites), no new backend contract. */
@Singleton
class FavoritesStore @Inject constructor(
    private val listingRepository: ListingRepository
) {
    var ids by mutableStateOf<Set<String>>(emptySet())
        private set

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun refresh() {
        scope.launch { ids = listingRepository.getFavoriteIds() }
    }

    /** Called by screens that changed a favourite through their own view model (detail, favourites list). */
    fun set(id: String, favorite: Boolean) {
        ids = if (favorite) ids + id else ids - id
    }

    fun clear() {
        ids = emptySet()
    }

    /** Optimistic toggle; reverted when the backend call fails. */
    fun toggle(id: String) {
        val wasFavorite = id in ids
        set(id, !wasFavorite)
        scope.launch {
            val ok = if (wasFavorite) listingRepository.removeFavorite(id) else listingRepository.addFavorite(id)
            if (!ok) set(id, wasFavorite)
        }
    }
}
