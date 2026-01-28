package com.example.zapas.ui.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zapas.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // Состояние для логина
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    // Состояние для регистрации
    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            val success = authRepository.login(username, password)

            _loginState.value = if (success) {
                LoginState.Success
            } else {
                LoginState.Error("Не удалось войти")
            }
        }
    }

    fun register(username: String, password: String, display_name: String) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading

            val success = authRepository.register(username, password, display_name)

            _registerState.value = if (success) {
                RegisterState.Success
            } else {
                RegisterState.Error("Не удалось зарегистрироваться")
            }
        }
    }

    suspend fun checkAuthStatus(): Boolean {
        return authRepository.isLoggedIn()
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}

// Состояния для UI
sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}