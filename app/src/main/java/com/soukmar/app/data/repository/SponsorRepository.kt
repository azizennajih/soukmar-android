package com.soukmar.app.data.repository

import com.soukmar.app.data.remote.ApiService
import com.soukmar.app.data.remote.dto.SponsorBannerDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/** Advertising/affiliate banners (mirrors the web's SponsorService). The app only has room for the INLINE
 * placement: between listings and on the home screen. Failures never matter to the visitor: no banners then. */
@Singleton
class SponsorRepository @Inject constructor(
    private val api: ApiService
) {
    private val cache = mutableMapOf<String, List<SponsorBannerDto>>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    suspend fun inlineBanners(country: String): List<SponsorBannerDto> {
        cache[country]?.let { return it }
        return try {
            val res = api.getBanners("INLINE", country)
            val list = if (res.isSuccessful) res.body() ?: emptyList() else emptyList()
            if (res.isSuccessful) cache[country] = list
            list
        } catch (e: Exception) { emptyList() }
    }

    fun click(id: String) {
        scope.launch { try { api.clickBanner(id) } catch (e: Exception) { /* a lost click count is harmless */ } }
    }
}
