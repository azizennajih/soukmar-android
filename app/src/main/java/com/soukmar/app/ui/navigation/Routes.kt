package com.soukmar.app.ui.navigation

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val RESET_PASSWORD = "reset_password?token={token}"
    fun resetPassword(token: String) = "reset_password?token=$token"
    const val HOME = "home"
    const val LISTINGS = "listings?category={category}&savedSearchId={savedSearchId}&editSearchId={editSearchId}"
    fun listings(category: String? = null, savedSearchId: String? = null, editSearchId: String? = null): String {
        val params = buildList {
            category?.let { add("category=$it") }
            savedSearchId?.let { add("savedSearchId=$it") }
            editSearchId?.let { add("editSearchId=$it") }
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
    const val ADMIN = "admin"
    const val LEGAL_NOTICE = "legal_notice"
    const val LEGAL_PRIVACY = "legal_privacy"
    const val LEGAL_TERMS = "legal_terms"
    const val LEGAL_WITHDRAWAL = "legal_withdrawal"
    const val SETTINGS = "settings"
    const val DELETE_ACCOUNT = "delete_account"
    const val IMAGE_SEARCH = "image_search"
    const val BOOST_LISTING = "boost_listing/{listingId}"
    fun boostListing(listingId: String) = "boost_listing/$listingId"
}
