package com.example.zapas.ui.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.zapas.ui.presentation.screens.auth.login.LoginScreen
import com.example.zapas.ui.presentation.screens.auth.register.RegisterScreen
import com.example.zapas.ui.presentation.screens.profile.ProfileScreen
import com.example.zapas.ui.presentation.screens.rooms.RoomsScreen
import com.example.zapas.ui.presentation.screens.splash.SplashScreen


@Composable
fun ZapasNavGraph(
    startDestination: String = Destinations.SPLASH
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController, startDestination = startDestination
    ) {
        composable(Destinations.SPLASH) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Destinations.LOGIN) {
                        // Убираем сплэш-экран из стека
                        popUpTo(Destinations.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate(Destinations.ROOMS) {
                        popUpTo(Destinations.SPLASH) { inclusive = true }
                    }
                }
            )
        }


        composable(Destinations.LOGIN) {
            LoginScreen(onLoginSuccess = {
                navController.navigate(Destinations.ROOMS) {
                    popUpTo(Destinations.LOGIN) { inclusive = true }
                }
            }, onNavigateToRegister = {
                navController.navigate(Destinations.REGISTER)
            })
        }

        composable(Destinations.REGISTER) {
            RegisterScreen(
                onRegisterComplete = {
                    navController.navigate(Destinations.ROOMS) {
                        popUpTo(Destinations.LOGIN) { inclusive = true }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Destinations.ROOMS) {
            RoomsScreen(
                onNavigateToProfile = {
                    navController.navigate(Destinations.PROFILE)
                }
            )
        }

        composable(Destinations.PROFILE) {
            ProfileScreen(
                onLogout = {
                    navController.navigate(Destinations.LOGIN) {
                        popUpTo(0)
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}