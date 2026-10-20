package com.example.zapas.features.auth.presentation.ui.register

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.zapas.features.auth.presentation.viewmodel.AuthViewModel
import com.example.zapas.features.auth.presentation.viewmodel.RegisterState

@Composable
fun RegisterScreen(
    onRegisterComplete: () -> Unit, onBack: () -> Unit, viewModel: AuthViewModel = hiltViewModel()
) {
    val usernameMutable = remember { mutableStateOf("") }
    val passwordMutable = remember { mutableStateOf("") }

    var currentStep by remember { mutableIntStateOf(1) }

    val registerState by viewModel.registerState.collectAsState()

    LaunchedEffect(registerState) {
        if (registerState is RegisterState.Success) {
            onRegisterComplete()
        }
    }

    when (currentStep) {
        1 -> RegisterStep1Screen(
            usernameMutable,
            passwordMutable,
            onNext = { error -> if (error == null) currentStep = 2 },
            onBack = onBack,
        )

        2 -> RegisterStep2Screen(
            username = usernameMutable.value,
            password = passwordMutable.value,
            onBack = { currentStep = 1 },
            onRegister = viewModel::register,
            registerState = registerState,
        )
    }
}

@Composable
fun RegisterStep1Screen(
    usernameMutable: MutableState<String>,
    passwordMutable: MutableState<String>,
    onNext: (String?) -> Unit,
    onBack: () -> Unit
) {
    var username by usernameMutable
    var password by passwordMutable

    var confirmPassword by remember { mutableStateOf("") }

    var error by remember { mutableStateOf<String?>(null) }

    fun setError() {
        error = when {
            username.length < 3 -> "Логин должен быть не менее 3 символов"
            password.length < 6 -> "Пароль должен быть не менее 6 символов"
            password != confirmPassword -> "Пароли не совпадают"
            else -> null
        }
    }

    RegisterStep1ScreenContent(
        username = username,
        password = password,
        confirmPassword = confirmPassword,
        onUsernameChange = {
            username = it
            setError()
        },
        onPasswordChange = {
            password = it
            setError()
        },
        onConfirmPasswordChange = {
            confirmPassword = it
            setError()
        },
        error = error,
        onNext = { onNext(error) },
        onBack = onBack
    )
}

@Composable
fun RegisterStep2Screen(
    username: String,
    password: String,
    onBack: () -> Unit,
    registerState: RegisterState,
    onRegister: (String, String, String, String) -> Unit
) {
    var displayName by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }

    RegisterStep2ScreenContent(
        username = username,
        password = password,
        bio = bio,
        onBioChange = { bio = it },
        displayName = displayName,
        onDisplayNameChange = { displayName = it },
        onBack = onBack,
        registerState,
        onRegister = onRegister,
    )
}
