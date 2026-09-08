package com.example.zapas.ui.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.zapas.ui.presentation.screens.auth.login.LoginScreen
import com.example.zapas.ui.presentation.screens.auth.register.RegisterScreen
import com.example.zapas.ui.presentation.screens.profile.ProfileScreen
import com.example.zapas.ui.presentation.screens.rooms.CreateRoomScreen
import com.example.zapas.ui.presentation.screens.rooms.RoomsScreen
import com.example.zapas.ui.presentation.screens.splash.SplashScreen


@Composable
fun ZapasNavGraph(
    startDestination: Any = Splash
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController, startDestination = startDestination
    ) {
        composable<Splash> {
            SplashScreen(onNavigateToLogin = {
                navController.navigate(Login) {
                    // Убираем сплэш-экран из стека
                    popUpTo<Splash> { inclusive = true }
                }
            }, onNavigateToMain = {
                navController.navigate(Rooms) {
                    popUpTo<Splash> { inclusive = true }
                }
            })
        }


        composable<Login> {
            LoginScreen(onLoginSuccess = {
                navController.navigate(Rooms) {
                    popUpTo<Login> { inclusive = true }
                }
            }, onNavigateToRegister = {
                navController.navigate(Register)
            })
        }

        composable<Register> {
            RegisterScreen(onRegisterComplete = {
                navController.navigate(Rooms) {
                    popUpTo<Login> { inclusive = true }
                }
            }, onBack = {
                navController.popBackStack()
            })
        }

        composable<Rooms> {
            RoomsScreen(
                onNavigateToProfile = {
                    navController.navigate(Profile)
                },
                onNavigateToCreateRoom = {
                    navController.navigate(CreateRoom)
                }
            )
        }

        composable<CreateRoom> {
            CreateRoomScreen(
                onBack = {
                    navController.popBackStack()
                },
                onSuccess = { roomId ->
                    // Go back to the list. In a real app, you might navigate to the room details.
                    navController.popBackStack()
                }
            )
        }

        composable<Profile> {
            ProfileScreen(onLogout = {
                navController.navigate(Login) {
                    popUpTo(0)
                }
            }, onBack = {
                navController.popBackStack()
            })
        }
    }
}