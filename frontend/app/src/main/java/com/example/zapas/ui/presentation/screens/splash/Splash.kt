package com.example.zapas.ui.presentation.screens.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.zapas.ui.presentation.viewmodels.splash.SplashViewModel

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToMain: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    // Собираем состояние как State
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()

    // При открытии экрана проверяем авторизацию
    LaunchedEffect(Unit) {
        viewModel.checkAuthStatus()
    }

    // Слушаем изменения состояния
    LaunchedEffect(isAuthenticated) {
        when (isAuthenticated) {
            true -> onNavigateToMain()
            false -> onNavigateToLogin()
            null -> { /* еще грузится */ }
        }
    }

    // Показываем индикатор загрузки
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}