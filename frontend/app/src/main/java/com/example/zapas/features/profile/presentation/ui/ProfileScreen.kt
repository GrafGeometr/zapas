package com.example.zapas.features.profile.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.zapas.features.profile.domain.model.User
import com.example.zapas.core.components.PreviewableAlertDialog
import com.example.zapas.features.profile.presentation.viewmodel.ProfileState
import com.example.zapas.features.profile.presentation.viewmodel.ProfileViewModel
import com.example.zapas.ui.theme.ZapasTheme

@Composable
fun ProfileScreen(
    onLogout: () -> Unit, onBack: () -> Unit, viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.passwordChangeSuccess) {
        if (state.passwordChangeSuccess) onLogout()
    }

    val actions = ProfileUiActions(
        onBack = onBack,
        onLogout = { viewModel.logout(onLogout) },
        onToggleEdit = viewModel::toggleEdit,
        onSave = viewModel::updateProfile,
        onShowPasswordDialog = { viewModel.setShowPasswordDialog(true) },
        onDismissPasswordDialog = { viewModel.setShowPasswordDialog(false) },
        onChangePassword = { old, new ->
            viewModel.changePassword(old, new)
        },
        onShowDeleteDialog = { viewModel.setShowDeleteDialog(true) },
        onDismissDeleteDialog = { viewModel.setShowDeleteDialog(false) },
        onDeleteConfirm = {
            viewModel.deleteAccount(onLogout)
        })

    ProfileRootUI(state = state, actions = actions)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileRootUI(
    state: ProfileState, actions: ProfileUiActions
) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.error, state.showPasswordDialog, state.showDeleteDialog) {
        if (!state.showPasswordDialog && !state.showDeleteDialog)
            state.error?.let { snackbarHostState.showSnackbar(it) }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ProfileTopBar(actions.onBack, actions.onLogout)
        }) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading && state.user == null -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                state.user != null -> {
                    ProfileScreenContent(
                        user = state.user, isEditing = state.isEditing, actions = actions, isLoading = state.isLoading
                    )
                }

                state.error != null -> {
                    ErrorMessage(
                        error = state.error, modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }

    if (state.showPasswordDialog) {
        ChangePasswordDialog(
            onDismiss = actions.onDismissPasswordDialog, onConfirm = actions.onChangePassword,
            isLoading = state.isLoading, error = state.error
        )
    }

    if (state.showDeleteDialog) {
        DeleteAccountDialog(
            onDismiss = actions.onDismissDeleteDialog, onConfirm = actions.onDeleteConfirm,
            isLoading = state.isLoading, error = state.error
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileTopBar(onBack: () -> Unit, onLogout: () -> Unit) {
    TopAppBar(title = { Text("Профиль") }, navigationIcon = {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
        }
    }, actions = {
        IconButton(onClick = onLogout) {
            Icon(Icons.Default.ExitToApp, contentDescription = "Выйти")
        }
    })
}

@Composable
private fun ErrorMessage(error: String, modifier: Modifier = Modifier) {
    Text(
        text = error, color = MaterialTheme.colorScheme.error, modifier = modifier.padding(16.dp)
    )
}

@Composable
fun ProfileScreenContent(
    user: User, isEditing: Boolean, actions: ProfileUiActions, isLoading: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProfileAvatar()

        Spacer(modifier = Modifier.height(16.dp))

        if (isEditing) {
            ProfileEditSection(user, actions, isLoading)
        } else {
            ProfileInfoSection(user, actions)
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))

//        Emails()

        ProfileActionsSection(actions, isLoading)
    }
}

@Composable
private fun ProfileAvatar() {
    Icon(
        Icons.Default.Person,
        contentDescription = null,
        modifier = Modifier.size(100.dp),
        tint = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun ProfileEditSection(user: User, actions: ProfileUiActions, isLoading: Boolean) {
    var displayName by remember(user.displayName) { mutableStateOf(user.displayName) }
    var bio by remember(user.bio) { mutableStateOf(user.bio) }

    OutlinedTextField(
        value = displayName,
        onValueChange = { displayName = it },
        label = { Text("Имя") },
        modifier = Modifier.fillMaxWidth(),
        enabled = !isLoading
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = bio,
        onValueChange = { bio = it },
        label = { Text("О себе") },
        modifier = Modifier.fillMaxWidth(),
        enabled = !isLoading
    )
    Spacer(modifier = Modifier.height(16.dp))
    Button(
        onClick = { actions.onSave(displayName, bio) },
        modifier = Modifier.fillMaxWidth(),
        enabled = !isLoading
    ) {
        Icon(Icons.Default.Check, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Сохранить")
    }
    TextButton(onClick = actions.onToggleEdit, enabled = !isLoading) {
        Text("Отмена")
    }
}

@Composable
private fun ProfileInfoSection(user: User, actions: ProfileUiActions) {
    Text(
        user.displayName,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold
    )
    Text(
        "@${user.username}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray
    )

    if (user.bio.isNotBlank()) {
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text(
                text = user.bio,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Button(onClick = actions.onToggleEdit, modifier = Modifier.fillMaxWidth()) {
        Icon(Icons.Default.Edit, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Редактировать профиль")
    }
}

@Composable
private fun ProfileActionsSection(actions: ProfileUiActions, isLoading: Boolean) {
    OutlinedButton(
        onClick = actions.onShowPasswordDialog, enabled = !isLoading, modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.Lock, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Сменить пароль")
    }

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedButton(
        onClick = actions.onShowDeleteDialog,
        enabled = !isLoading,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
    ) {
        Icon(Icons.Default.Delete, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Удалить аккаунт")
    }
}

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit, onConfirm: (String, String) -> Unit,
    isLoading: Boolean = false, error: String? = null
) {
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }

    PreviewableAlertDialog(onDismissRequest = { if (!isLoading) onDismiss() }, title = { Text("Смена пароля") }, text = {
        Column {
            OutlinedTextField(
                value = oldPass,
                onValueChange = { oldPass = it },
                label = { Text("Старый пароль") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = newPass,
                onValueChange = { newPass = it },
                label = { Text("Новый пароль") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = confirmPass,
                onValueChange = { confirmPass = it },
                label = { Text("Подтвердите новый пароль") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }, confirmButton = {
        Button(
            onClick = { onConfirm(oldPass, newPass) },
            enabled = !isLoading && oldPass.isNotBlank() && newPass.isNotBlank() && newPass == confirmPass
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp)) else Text("Сменить")
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss, enabled = !isLoading) {
            Text("Отмена")
        }
    })
}

@Composable
fun DeleteAccountDialog(
    onDismiss: () -> Unit, onConfirm: () -> Unit,
    isLoading: Boolean = false, error: String? = null
) {
    PreviewableAlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Удалить аккаунт?") },
        text = {
            Column {
                Text("Это действие необратимо. Все ваши данные будут удалены.")
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isLoading,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp)) else Text("Удалить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Отмена")
            }
        })
}

// --- Previews ---

class ProfileStateProvider : PreviewParameterProvider<ProfileState> {
    private val mockUser = User(
        id = "1",
        username = "bob_ross",
        displayName = "Bob Ross",
        bio = "Famous Painter",
        email = "bob@ross.com",
        verifiedEmail = true,
        createdAt = "2023-01-01T00:00:00Z"
    )

    override val values = sequenceOf(
        ProfileState(
            user = mockUser, isLoading = false
        ), ProfileState(
            user = mockUser, isLoading = false, isEditing = true
        ), ProfileState(
            user = mockUser, showPasswordDialog = true
        ), ProfileState(
            user = mockUser, showPasswordDialog = true, isLoading = true
        ), ProfileState(
            user = mockUser, showPasswordDialog = true, error = "Неверный старый пароль"
        ), ProfileState(
            user = mockUser, showDeleteDialog = true, isLoading = true
        ), ProfileState(
            user = mockUser, showDeleteDialog = true, error = "Сервер не поддерживает удаление аккаунта"
        ), ProfileState(
            isLoading = true
        ), ProfileState(
            error = "Ошибка загрузки профиля"
        )
    )
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview(
    @PreviewParameter(ProfileStateProvider::class) state: ProfileState
) {
    ZapasTheme {
        ProfileRootUI(
            state = state, actions = ProfileUiActions()
        )
    }
}
