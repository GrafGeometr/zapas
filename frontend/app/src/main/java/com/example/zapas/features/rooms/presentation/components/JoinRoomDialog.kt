package com.example.zapas.features.rooms.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zapas.core.components.PreviewableAlertDialog
import com.example.zapas.features.rooms.presentation.viewmodel.RoomsUiState
import com.example.zapas.ui.theme.ZapasTheme

@Composable
fun JoinRoomDialog(
    uiState: RoomsUiState,
    onDismiss: () -> Unit,
    onSetInviteToken: (String) -> Unit,
    onJoinRoom: (String) -> Unit,
    onCreateRoomClick: () -> Unit
) {
    PreviewableAlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Присоединиться к комнате")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = uiState.inviteTokenInput,
                    onValueChange = onSetInviteToken,
                    label = { Text("Инвайт-токен") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !uiState.isLoading
                )

                if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                TextButton(
                    onClick = onCreateRoomClick,
                    enabled = !uiState.isLoading,
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Text("Нет кода? Создать комнату")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onJoinRoom(uiState.inviteTokenInput) },
                enabled = !uiState.isLoading && uiState.inviteTokenInput.isNotBlank()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Войти")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !uiState.isLoading
            ) {
                Text("Отмена")
            }
        }
    )
}

@Preview(showBackground = true, name = "Диалог - Вход по инвайту")
@Composable
fun JoinRoomDialogPreview() {
    ZapasTheme {
        JoinRoomDialog(
            uiState = RoomsUiState(showJoinDialog = true, inviteTokenInput = "ABC-123"),
            onDismiss = {},
            onSetInviteToken = {},
            onJoinRoom = {},
            onCreateRoomClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Диалог - Ошибка")
@Composable
fun JoinRoomDialogErrorPreview() {
    ZapasTheme {
        JoinRoomDialog(
            uiState = RoomsUiState(showJoinDialog = true, inviteTokenInput = "INVALID", errorMessage = "Неверный инвайт-токен"),
            onDismiss = {},
            onSetInviteToken = {},
            onJoinRoom = {},
            onCreateRoomClick = {}
        )
    }
}
