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
import com.soukmar.app.ui.screens.deposerannonce.DeposerAnnonceScreen
import com.soukmar.app.ui.screens.home.HomeScreen
import com.soukmar.app.ui.screens.listingdetail.ListingDetailScreen
import com.soukmar.app.ui.screens.listings.ListingsScreen

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
                onOpenDeposerAnnonce = { navController.navigate(Routes.deposerAnnonce()) }
            )
        }
        composable(
            route = Routes.LISTINGS,
            arguments = listOf(navArgument("category") { type = NavType.StringType; nullable = true; defaultValue = null })
        ) { backStackEntry ->
            ListingsScreen(
                initialCategory = backStackEntry.arguments?.getString("category"),
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
                onRequireLogin = { navController.navigate(Routes.LOGIN) }
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
    }
}
