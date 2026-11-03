package com.example.zapas.features.auth.presentation.ui.register

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.zapas.features.auth.presentation.viewmodel.AuthViewModel
import com.example.zapas.features.auth.presentation.viewmodel.RegisterState

@Composable
fun RegisterScreen(
    onRegisterComplete: () -> Unit, onBack: () -> Unit, viewModel: AuthViewModel = hiltViewModel()
) {
    var username by rememberSaveable { mutableStateOf("") }
    var displayName by rememberSaveable { mutableStateOf("") }
    var bio by rememberSaveable { mutableStateOf("") }
    var currentStep by rememberSaveable { mutableIntStateOf(1) }
    var showValidation by rememberSaveable { mutableStateOf(false) }
    val credentials by viewModel.registrationCredentials.collectAsState()
    val registerState by viewModel.registerState.collectAsState()
    val validationError = when {
        username.length < 3 -> "Логин должен быть не менее 3 символов"
        credentials.password.length < 6 -> "Пароль должен быть не менее 6 символов"
        credentials.password != credentials.confirmation -> "Пароли не совпадают"
        else -> null
    }

    LaunchedEffect(registerState) {
        if (registerState is RegisterState.Success) {
            viewModel.clearRegistrationCredentials()
            onRegisterComplete()
        }
    }
    LaunchedEffect(credentials.password) {
        if (credentials.password.isEmpty()) currentStep = 1
    }

    when (currentStep) {
        1 -> RegisterStep1ScreenContent(
            username = username,
            password = credentials.password,
            confirmPassword = credentials.confirmation,
            onUsernameChange = {
                username = it
                showValidation = true
            },
            onPasswordChange = {
                viewModel.setRegistrationPassword(it)
                showValidation = true
            },
            onConfirmPasswordChange = {
                viewModel.setRegistrationConfirmation(it)
                showValidation = true
            },
            error = validationError.takeIf { showValidation },
            onNext = {
                showValidation = true
                if (validationError == null) currentStep = 2
            },
            onBack = onBack
        )
        2 -> RegisterStep2ScreenContent(
            username = username,
            password = credentials.password,
            onBack = { currentStep = 1 },
            registerState = registerState,
            onRegister = viewModel::register,
            displayName = displayName,
            bio = bio,
            onDisplayNameChange = { displayName = it },
            onBioChange = { bio = it }
        )
    }
}
