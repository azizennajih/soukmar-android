package com.soukmar.app.ui.navigation

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val RESET_PASSWORD = "reset_password?token={token}"
    fun resetPassword(token: String) = "reset_password?token=$token"
    const val HOME = "home"
    const val LISTINGS = "listings?category={category}&savedSearchId={savedSearchId}"
    fun listings(category: String? = null, savedSearchId: String? = null): String {
        val params = buildList {
            category?.let { add("category=$it") }
            savedSearchId?.let { add("savedSearchId=$it") }
        }
        return if (params.isEmpty()) "listings" else "listings?" + params.joinToString("&")
    }
    const val LISTING_DETAIL = "listing/{id}"
    fun listingDetail(id: String) = "listing/$id"
    const val DEPOSER_ANNONCE = "deposer_annonce?id={id}"
    fun deposerAnnonce(id: String? = null) = if (id != null) "deposer_annonce?id=$id" else "deposer_annonce"
    const val CHAT_LIST = "chat"
    const val CHAT_DETAIL = "chat/{conversationId}"
    fun chatDetail(conversationId: String) = "chat/$conversationId"
    const val MES_ANNONCES = "mes_annonces"
    const val FAVORIS = "favoris"
    const val PROFIL = "profil"
    const val SELLER_PROFILE = "vendeur/{id}"
    fun sellerProfile(id: String) = "vendeur/$id"
    const val SAVED_SEARCHES = "recherches_sauvegardees"
    const val NOTIFICATIONS = "notifications"
}
