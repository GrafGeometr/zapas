package com.example.zapas.ui.presentation.viewmodels.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.example.zapas.domain.models.AppError
import com.example.zapas.domain.usecase.auth.CheckAuthStatusUseCase
import com.example.zapas.domain.usecase.auth.LoginParams
import com.example.zapas.domain.usecase.auth.LoginUseCase
import com.example.zapas.domain.usecase.auth.LogoutUseCase
import com.example.zapas.domain.usecase.auth.RegisterParams
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

            when (val result = loginUseCase(LoginParams(username, password))) {
                is Either.Right -> _loginState.value = LoginState.Success
                is Either.Left -> _loginState.value = LoginState.Error(result.value.toUserMessage())
            }
        }
    }

    fun register(username: String, password: String, displayName: String, bio: String) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading

            when (val result = registerUseCase(RegisterParams(username, password, displayName, bio))) {
                is Either.Right -> _registerState.value = RegisterState.Success
                is Either.Left -> _registerState.value = RegisterState.Error(result.value.toUserMessage())
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

    private fun AppError.toUserMessage(): String {
        return when (this) {
            is AppError.ApiError -> when (code) {
                401 -> "Неверный логин или пароль"
                409 -> "Пользователь с таким именем уже существует"
                else -> message
            }
            is AppError.NetworkError -> "Ошибка сети. Проверьте подключение к интернету"
            is AppError.UnknownError -> message ?: "Произошла неизвестная ошибка"
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