package com.example.zapas.features.auth.presentation.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zapas.features.auth.presentation.viewmodel.LoginState
import com.example.zapas.ui.theme.ZapasTheme


@Composable
fun LoginScreenContent(
    loginState: LoginState,
    onLogin: (String, String) -> Unit,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Zapas", style = MaterialTheme.typography.headlineLarge)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Логин") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onLogin(username, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = loginState !is LoginState.Loading
        ) {
            if (loginState is LoginState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp
                )
            } else {
                Text("Войти")
            }
        }

        if (loginState is LoginState.Error) {
            Text(
                text = loginState.message,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToRegister) {
            Text("Нет аккаунта? Зарегистрироваться")
        }
    }
}

@Preview(showBackground = true, name = "Idle")
@Composable
fun LoginScreenIdlePreview() {
    ZapasTheme {
        LoginScreenContent(
            loginState = LoginState.Idle,
            onLogin = { _, _ -> },
            onNavigateToRegister = {})
    }
}

@Preview(showBackground = true, name = "Loading")
@Composable
fun LoginScreenLoadingPreview() {
    ZapasTheme {
        LoginScreenContent(
            loginState = LoginState.Loading,
            onLogin = { _, _ -> },
            onNavigateToRegister = {})
    }
}

@Preview(showBackground = true, name = "Error")
@Composable
fun LoginScreenErrorPreview() {
    ZapasTheme {
        LoginScreenContent(
            loginState = LoginState.Error("Неверный логин или пароль"),
            onLogin = { _, _ -> },
            onNavigateToRegister = {})
    }
}
