package com.soukmar.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.soukmar.app.ui.screens.auth.ForgotPasswordScreen
import com.soukmar.app.ui.screens.auth.LoginScreen
import com.soukmar.app.ui.screens.auth.RegisterScreen
import com.soukmar.app.ui.screens.auth.ResetPasswordScreen
import com.soukmar.app.ui.screens.chat.ChatListScreen
import com.soukmar.app.ui.screens.chat.ChatScreen
import com.soukmar.app.ui.screens.deposerannonce.DeposerAnnonceScreen
import com.soukmar.app.ui.screens.favoris.FavorisScreen
import com.soukmar.app.ui.screens.home.HomeScreen
import com.soukmar.app.ui.screens.profil.ProfilScreen
import com.soukmar.app.ui.screens.listingdetail.ListingDetailScreen
import com.soukmar.app.ui.screens.listings.ListingsScreen
import com.soukmar.app.ui.screens.mesannonces.MesAnnoncesScreen
import com.soukmar.app.ui.screens.sellerprofile.SellerProfileScreen
import com.soukmar.app.ui.screens.savedsearches.SavedSearchesScreen
import com.soukmar.app.ui.screens.notifications.NotificationsScreen
import com.soukmar.app.ui.screens.admin.AdminScreen

@Composable
fun SoukMarNavGraph(startDestination: String) {
    val navController: NavHostController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onNavigateToForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) }
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(onNavigateToLogin = { navController.popBackStack() })
        }
        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(onBackToLogin = { navController.popBackStack() })
        }
        composable(
            route = Routes.RESET_PASSWORD,
            arguments = listOf(navArgument("token") { type = NavType.StringType; nullable = true; defaultValue = null })
        ) { backStackEntry ->
            ResetPasswordScreen(
                token = backStackEntry.arguments?.getString("token"),
                onDone = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.RESET_PASSWORD) { inclusive = true }
                    }
                },
                onRequestNewLink = { navController.navigate(Routes.FORGOT_PASSWORD) }
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                onLoggedOut = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onOpenCategory = { category -> navController.navigate(Routes.listings(category)) },
                onOpenSearch = { navController.navigate(Routes.listings()) },
                onOpenDeposerAnnonce = { navController.navigate(Routes.deposerAnnonce()) },
                onOpenChat = { navController.navigate(Routes.CHAT_LIST) },
                onOpenMesAnnonces = { navController.navigate(Routes.MES_ANNONCES) },
                onOpenFavoris = { navController.navigate(Routes.FAVORIS) },
                onOpenProfil = { navController.navigate(Routes.PROFIL) },
                onOpenSavedSearches = { navController.navigate(Routes.SAVED_SEARCHES) },
                onOpenNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                onOpenAdmin = { navController.navigate(Routes.ADMIN) }
            )
        }
        composable(
            route = Routes.LISTINGS,
            arguments = listOf(
                navArgument("category") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("savedSearchId") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            ListingsScreen(
                initialCategory = backStackEntry.arguments?.getString("category"),
                savedSearchId = backStackEntry.arguments?.getString("savedSearchId"),
                onBack = { navController.popBackStack() },
                onOpenListing = { id -> navController.navigate(Routes.listingDetail(id)) }
            )
        }
        composable(
            route = Routes.LISTING_DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            ListingDetailScreen(
                listingId = backStackEntry.arguments?.getString("id") ?: "",
                onBack = { navController.popBackStack() },
                onRequireLogin = { navController.navigate(Routes.LOGIN) },
                onOpenChat = { conversationId -> navController.navigate(Routes.chatDetail(conversationId)) },
                onOpenSeller = { sellerId -> navController.navigate(Routes.sellerProfile(sellerId)) }
            )
        }
        composable(
            route = Routes.DEPOSER_ANNONCE,
            arguments = listOf(navArgument("id") { type = NavType.StringType; nullable = true; defaultValue = null })
        ) { backStackEntry ->
            DeposerAnnonceScreen(
                editId = backStackEntry.arguments?.getString("id"),
                onBack = { navController.popBackStack() },
                onRequireLogin = { navController.navigate(Routes.LOGIN) },
                onPublished = { id ->
                    navController.navigate(Routes.listingDetail(id)) {
                        popUpTo(Routes.DEPOSER_ANNONCE) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.CHAT_LIST) {
            ChatListScreen(
                onBack = { navController.popBackStack() },
                onOpenConversation = { conversationId -> navController.navigate(Routes.chatDetail(conversationId)) }
            )
        }
        composable(
            route = Routes.CHAT_DETAIL,
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
        ) { backStackEntry ->
            ChatScreen(
                conversationId = backStackEntry.arguments?.getString("conversationId") ?: "",
                onBack = { navController.popBackStack() },
                onOpenListing = { id -> navController.navigate(Routes.listingDetail(id)) }
            )
        }
        composable(Routes.MES_ANNONCES) {
            MesAnnoncesScreen(
                onBack = { navController.popBackStack() },
                onOpenListing = { id -> navController.navigate(Routes.listingDetail(id)) },
                onEditListing = { id -> navController.navigate(Routes.deposerAnnonce(id)) },
                onNewListing = { navController.navigate(Routes.deposerAnnonce()) }
            )
        }
        composable(Routes.FAVORIS) {
            FavorisScreen(
                onBack = { navController.popBackStack() },
                onOpenListing = { id -> navController.navigate(Routes.listingDetail(id)) },
                onBrowse = {
                    navController.navigate(Routes.listings()) {
                        popUpTo(Routes.FAVORIS) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.PROFIL) {
            ProfilScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.SELLER_PROFILE,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            SellerProfileScreen(
                sellerId = backStackEntry.arguments?.getString("id") ?: "",
                onBack = { navController.popBackStack() },
                onOpenListing = { id -> navController.navigate(Routes.listingDetail(id)) }
            )
        }
        composable(Routes.SAVED_SEARCHES) {
            SavedSearchesScreen(
                onBack = { navController.popBackStack() },
                onOpenSearch = { savedSearchId -> navController.navigate(Routes.listings(savedSearchId = savedSearchId)) }
            )
        }
        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(
                onBack = { navController.popBackStack() },
                onOpenChat = { conversationId -> navController.navigate(Routes.chatDetail(conversationId)) },
                onOpenListing = { id -> navController.navigate(Routes.listingDetail(id)) },
                onOpenProfil = { navController.navigate(Routes.PROFIL) }
            )
        }
        composable(Routes.ADMIN) {
            AdminScreen(
                onBack = { navController.popBackStack() },
                onOpenListing = { id -> navController.navigate(Routes.listingDetail(id)) }
            )
        }
    }
}
