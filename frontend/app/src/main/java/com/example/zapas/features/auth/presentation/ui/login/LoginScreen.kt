package com.example.zapas.features.auth.presentation.ui.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.zapas.features.auth.presentation.viewmodel.AuthViewModel
import com.example.zapas.features.auth.presentation.viewmodel.LoginState


@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val loginState by viewModel.loginState.collectAsState()

    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            onLoginSuccess()
        }
    }

    LoginScreenContent(
        loginState = loginState,
        onLogin = viewModel::login,
        onNavigateToRegister = onNavigateToRegister
    )
}

