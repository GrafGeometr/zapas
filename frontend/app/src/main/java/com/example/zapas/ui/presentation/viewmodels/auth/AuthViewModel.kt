package com.example.zapas.ui.presentation.viewmodels.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zapas.domain.usecase.auth.CheckAuthStatusUseCase
import com.example.zapas.domain.usecase.auth.LoginParams
import com.example.zapas.domain.usecase.auth.LoginResult
import com.example.zapas.domain.usecase.auth.LoginUseCase
import com.example.zapas.domain.usecase.auth.LogoutUseCase
import com.example.zapas.domain.usecase.auth.RegisterParams
import com.example.zapas.domain.usecase.auth.RegisterResult
import com.example.zapas.domain.usecase.auth.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val checkAuthStatusUseCase: CheckAuthStatusUseCase,
    private val logoutUseCase: LogoutUseCase
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

            val result = loginUseCase(LoginParams(username, password))

            _loginState.value = if (result is LoginResult.Success) {
                LoginState.Success
            } else {
                LoginState.Error("Не удалось войти")
            }
        }
    }

    fun register(username: String, password: String, display_name: String) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading

            val result = registerUseCase(RegisterParams(username, password, display_name))

            _registerState.value = if (result is RegisterResult.Success) {
                RegisterState.Success
            } else {
                RegisterState.Error("Не удалось зарегистрироваться")
            }
        }
    }

    suspend fun checkAuthStatus(): Boolean {
        return checkAuthStatusUseCase()
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
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