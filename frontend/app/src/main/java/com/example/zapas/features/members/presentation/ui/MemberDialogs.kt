package com.example.zapas.features.members.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.zapas.core.components.PreviewableAlertDialog
import com.example.zapas.features.members.presentation.viewmodel.MemberProfileState

@Composable
fun MemberProfileDialog(
    state: MemberProfileState,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var displayName by rememberSaveable {
        mutableStateOf(state.localProfile?.displayName ?: state.member?.displayName.orEmpty())
    }
    var bio by rememberSaveable {
        mutableStateOf(state.localProfile?.bio ?: state.member?.bio.orEmpty())
    }
    MemberProfileDialogContent(
        state, displayName, bio, { displayName = it }, { bio = it },
        onDismiss, { onSave(displayName, bio) }
    )
}

@Composable
fun MemberProfileDialogContent(
    state: MemberProfileState,
    displayName: String,
    bio: String,
    onDisplayNameChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    PreviewableAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Профиль в комнате") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Изменения относятся только к этой комнате.")
                OutlinedTextField(
                    value = displayName,
                    onValueChange = onDisplayNameChange,
                    label = { Text("Имя в комнате") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isSaving,
                    isError = displayName.isBlank()
                )
                OutlinedTextField(
                    value = bio,
                    onValueChange = onBioChange,
                    label = { Text("Описание в комнате") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSaving
                )
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = displayName.isNotBlank() && !state.isSaving
            ) {
                if (state.isSaving) CircularProgressIndicator(modifier = Modifier.size(24.dp)) else Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !state.isSaving) { Text("Отмена") }
        }
    )
}
