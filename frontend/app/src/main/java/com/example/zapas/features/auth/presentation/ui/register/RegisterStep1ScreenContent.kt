package com.example.zapas.features.auth.presentation.ui.register

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zapas.ui.theme.ZapasTheme


@Composable
fun RegisterStep1ScreenContent(
    username: String,
    password: String,
    confirmPassword: String,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    error: String?,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Кнопка назад
        IconButton(
            onClick = onBack, modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
        }

        Text(
            "Шаг 1 из 2",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            "Создайте учётную запись",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = username,
            onValueChange = { onUsernameChange(it) },
            label = { Text("Логин") },
            modifier = Modifier.fillMaxWidth(),
            isError = error != null
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { onPasswordChange(it) },
            label = { Text("Пароль") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            isError = error != null
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { onConfirmPasswordChange(it) },
            label = { Text("Подтвердите пароль") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            isError = error != null
        )

        error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { onNext() },
            modifier = Modifier.fillMaxWidth(),
            enabled = username.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank()
        ) {
            Text("Далее")
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }
}


@Preview(showBackground = true, name = "Step1")
@Composable
fun RegisterStep1ScreenPreview() {
    ZapasTheme {
        RegisterStep1ScreenContent(
            username = "GGFu87",
            password = "I don't know",
            onUsernameChange = {},
            onPasswordChange = {},
            onNext = {},
            onBack = { },
            confirmPassword = "I don't know",
            onConfirmPasswordChange = {},
            error = null
        )
    }
}

@Preview(showBackground = true, name = "Step1 Error")
@Composable
fun RegisterStep1ScreenErrorPreview() {
    ZapasTheme {
        RegisterStep1ScreenContent(
            username = "GGFu87",
            password = "I don't know",
            onUsernameChange = {},
            onPasswordChange = {},
            onNext = {},
            onBack = { },
            confirmPassword = "Passowrd",
            onConfirmPasswordChange = {},
            error = "Пароли не совпадают"
        )
    }
}
