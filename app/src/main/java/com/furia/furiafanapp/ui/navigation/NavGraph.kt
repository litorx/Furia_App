package com.furia.furiafanapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.furia.furiafanapp.ui.screens.HomeScreen
import com.furia.furiafanapp.ui.screens.OnboardingScreen
import com.furia.furiafanapp.ui.screens.LoginScreen
import com.furia.furiafanapp.ui.screens.RegisterScreen
import com.furia.furiafanapp.ui.screens.ProfileSetupScreen
import com.furia.furiafanapp.ui.screens.ProfileScreen
import com.furia.furiafanapp.ui.screens.ChatBotScreen
import com.furia.furiafanapp.ui.screens.ArenaScreen
import com.furia.furiafanapp.ui.screens.MiniGamesScreen
import com.furia.furiafanapp.ui.screens.ShopScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ProfileSetup : Screen("profile_setup")
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Profile : Screen("profile")
    object ChatBot : Screen("chat_bot")
    object Arena : Screen("arena")
    object MiniGames : Screen("mini_games")
    object Shop : Screen("shop")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.ProfileSetup.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ProfileSetup.route) {
            ProfileSetupScreen(
                onProfileSaved = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.ProfileSetup.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onOnboardingComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController)
        }
        composable(Screen.ChatBot.route) {
            ChatBotScreen(navController = navController)
        }
        composable(Screen.Arena.route) {
            ArenaScreen(navController = navController)
        }
        composable(Screen.MiniGames.route) {
            MiniGamesScreen(navController = navController)
        }
        composable(Screen.Shop.route) {
            ShopScreen(navController = navController)
        }
    }
}