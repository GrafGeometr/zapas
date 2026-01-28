package com.example.zapas.ui.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.zapas.ui.presentation.screens.auth.LoginScreen
import com.example.zapas.ui.presentation.screens.auth.RegisterScreen
import com.example.zapas.ui.presentation.screens.auth.RegisterStep1Screen
import com.example.zapas.ui.presentation.screens.auth.RegisterStep2Screen
import com.example.zapas.ui.presentation.screens.main.MainScreen


@Composable
fun ZapasNavGraph(
    startDestination: String = Destinations.LOGIN
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController, startDestination = startDestination
    ) {
        composable(Destinations.LOGIN) {
            LoginScreen(onLoginSuccess = {
                navController.navigate(Destinations.MAIN) {
                    popUpTo(Destinations.LOGIN) { inclusive = true }
                }
            }, onNavigateToRegister = {
                navController.navigate(Destinations.REGISTER_STEP1)
            })
        }

        composable("register") {
            RegisterScreen(
                onRegisterComplete = {
                    // После регистрации возвращаемся на экран логина
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Destinations.REGISTER_STEP2,
            arguments = listOf(
                navArgument("username") { type = NavType.StringType },
                navArgument("password") { type = NavType.StringType })
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            val password = backStackEntry.arguments?.getString("password") ?: ""

            RegisterStep2Screen(username = username, password = password, onRegisterComplete = {
                navController.navigate(Destinations.MAIN) {
                    popUpTo(0)
                }
            }, onBack = {
                navController.popBackStack()
            })
        }

        composable(Destinations.MAIN) {
            MainScreen(
                onLogout = {
                    navController.navigate(Destinations.LOGIN) {
                        popUpTo(0)
                    }
                })
        }
    }
}