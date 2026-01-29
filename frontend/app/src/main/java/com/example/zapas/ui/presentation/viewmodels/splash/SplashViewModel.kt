package com.example.zapas.ui.presentation.viewmodels.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zapas.domain.usecase.auth.CheckAuthStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val checkAuthStatusUseCase: CheckAuthStatusUseCase
) : ViewModel() {

    // null - еще загружается, true - авторизован, false - не авторизован
    private val _isAuthenticated = MutableStateFlow<Boolean?>(null)
    val isAuthenticated: StateFlow<Boolean?> = _isAuthenticated

    fun checkAuthStatus() {
        viewModelScope.launch {
            val isLoggedIn = checkAuthStatusUseCase()
            _isAuthenticated.value = isLoggedIn
        }
    }
}