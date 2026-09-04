package com.soukmar.app.ui.navigation

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val RESET_PASSWORD = "reset_password?token={token}"
    fun resetPassword(token: String) = "reset_password?token=$token"
    const val HOME = "home"
    const val LISTINGS = "listings?category={category}"
    fun listings(category: String? = null) = if (category != null) "listings?category=$category" else "listings"
    const val LISTING_DETAIL = "listing/{id}"
    fun listingDetail(id: String) = "listing/$id"
    const val DEPOSER_ANNONCE = "deposer_annonce?id={id}"
    fun deposerAnnonce(id: String? = null) = if (id != null) "deposer_annonce?id=$id" else "deposer_annonce"
}
