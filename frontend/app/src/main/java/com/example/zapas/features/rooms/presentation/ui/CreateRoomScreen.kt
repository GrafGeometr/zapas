package com.example.zapas.features.rooms.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.zapas.features.rooms.presentation.viewmodel.CreateRoomState
import com.example.zapas.features.rooms.presentation.viewmodel.CreateRoomViewModel
import com.example.zapas.ui.theme.ZapasTheme

@Composable
fun CreateRoomScreen(
    onBack: () -> Unit,
    onSuccess: (String) -> Unit,
    viewModel: CreateRoomViewModel = hiltViewModel()
) {
    val createRoomState by viewModel.state.collectAsState()

    var displayName by rememberSaveable { mutableStateOf("") }
    var bio by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(createRoomState) {
        if (createRoomState is CreateRoomState.Success) {
            onSuccess((createRoomState as CreateRoomState.Success).id)
        }
    }

    CreateRoomScreenContent(
        onBack = onBack,
        onSubmit = {
            viewModel.createRoom(displayName, bio)
        },
        onEditDisplayName = { displayName = it },
        onEditBio = { bio = it },
        bio = bio,
        displayName = displayName,
        state = createRoomState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoomScreenContent(
    onBack: () -> Unit,
    onSubmit: () -> Unit,
    onEditDisplayName: (String) -> Unit,
    onEditBio: (String) -> Unit,
    bio: String,
    displayName: String,
    state: CreateRoomState
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Создать комнату") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = displayName,
                onValueChange = onEditDisplayName,
                label = { Text("Название") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = state !is CreateRoomState.Loading
            )

            OutlinedTextField(
                value = bio,
                onValueChange = onEditBio,
                label = { Text("Описание") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                enabled = state !is CreateRoomState.Loading
            )

            Spacer(modifier = Modifier.weight(1f))

            if (state is CreateRoomState.Error) {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                enabled = state !is CreateRoomState.Loading && displayName.isNotBlank()
            ) {
                if (state is CreateRoomState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Создать")
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Idle")
@Composable
fun CreateRoomScreenIdlePreview() {
    ZapasTheme {
        CreateRoomScreenContent(
            onBack = {},
            onSubmit = {},
            onEditDisplayName = {},
            onEditBio = {},
            bio = "",
            displayName = "",
            state = CreateRoomState.Idle
        )
    }
}

@Preview(showBackground = true, name = "Loading")
@Composable
fun CreateRoomScreenLoadingPreview() {
    ZapasTheme {
        CreateRoomScreenContent(
            onBack = {},
            onSubmit = {},
            onEditDisplayName = {},
            onEditBio = {},
            bio = "Some bio description",
            displayName = "New Room Name",
            state = CreateRoomState.Loading
        )
    }
}

@Preview(showBackground = true, name = "Error")
@Composable
fun CreateRoomScreenErrorPreview() {
    ZapasTheme {
        CreateRoomScreenContent(
            onBack = {},
            onSubmit = {},
            onEditDisplayName = {},
            onEditBio = {},
            bio = "Failed bio",
            displayName = "Failed Room",
            state = CreateRoomState.Error("Что-то пошло не так. Попробуйте снова.")
        )
    }
}
