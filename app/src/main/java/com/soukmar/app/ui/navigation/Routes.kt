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
}
