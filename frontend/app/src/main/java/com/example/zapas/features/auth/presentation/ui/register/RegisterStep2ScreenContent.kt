package com.example.zapas.features.auth.presentation.ui.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zapas.features.auth.presentation.viewmodel.RegisterState
import com.example.zapas.ui.theme.ZapasTheme


@Composable
fun RegisterStep2ScreenContent(
    username: String,
    password: String,
    bio: String,
    onBioChange: (String) -> Unit,
    displayName: String,
    onDisplayNameChange: (String) -> Unit,
    onBack: () -> Unit,
    registerState: RegisterState,
    onRegister: (String, String, String, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        IconButton(
            onClick = onBack, modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
        }

        Text(
            "Шаг 2 из 2",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            "Расскажите о себе",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Показываем логин (только для информации)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Ваш логин", style = MaterialTheme.typography.labelSmall)
                    Text(username, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        OutlinedTextField(
            value = displayName,
            onValueChange = { onDisplayNameChange(it) },
            label = { Text("Отображаемое имя") },
            modifier = Modifier.fillMaxWidth(),
            isError = registerState is RegisterState.Error
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = bio,
            onValueChange = onBioChange,
            label = { Text("О себе") },
            modifier = Modifier.fillMaxWidth(),
            isError = registerState is RegisterState.Error
        )

        if (registerState is RegisterState.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = registerState.message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                if (displayName.isNotBlank()) {
                    onRegister(username, password, displayName, bio)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = displayName.isNotBlank() && registerState !is RegisterState.Loading
        ) {
            if (registerState is RegisterState.Loading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Регистрация...")
                }
            } else {
                Text("Зарегистрироваться")
            }
        }
    }
}

@Preview(showBackground = true, name = "Step2")
@Composable
fun RegisterStep2ScreenPreview() {
    ZapasTheme {
        RegisterStep2ScreenContent(
            username = "GGFu87",
            password = "I don't know",
            onBack = {},
            registerState = RegisterState.Idle,
            onRegister = { _, _, _, _ -> },
            bio = "Aboba",
            onBioChange = { },
            displayName = "Ouyrenak",
            onDisplayNameChange = {},
        )
    }
}

@Preview(showBackground = true, name = "Step2 Loading")
@Composable
fun RegisterStep2ScreenLoadingPreview() {
    ZapasTheme {
        RegisterStep2ScreenContent(
            username = "GGFu87",
            password = "I don't know",
            onBack = {},
            registerState = RegisterState.Loading,
            onRegister = { _, _, _, _ -> },
            bio = "Aboba",
            onBioChange = { },
            displayName = "Ouyrenak",
            onDisplayNameChange = {},
        )
    }
}

@Preview(showBackground = true, name = "Step2 Error")
@Composable
fun RegisterStep2ScreenErrorPreview() {
    ZapasTheme {
        RegisterStep2ScreenContent(
            username = "GGFu87",
            password = "I don't know",
            onBack = {},
            registerState = RegisterState.Error("Занято"),
            onRegister = { _, _, _, _ -> },
            bio = "Aboba",
            onBioChange = { },
            displayName = "Ouyrenak",
            onDisplayNameChange = {},
        )
    }
}


