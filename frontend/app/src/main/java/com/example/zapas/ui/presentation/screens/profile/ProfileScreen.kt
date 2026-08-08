package com.example.zapas.ui.presentation.screens.profile

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.zapas.ui.presentation.viewmodels.profile.ProfileViewModel

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    Button(
        onClick = {
            // Вызываем logout из ViewModel
            viewModel.logout(onLogout)
        }, modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Выйти")
    }
}