// ui/presentation/screens/main/MainScreen.kt
package com.example.zapas.ui.presentation.screens.rooms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.zapas.ui.presentation.viewmodels.profile.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomsScreen() {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Мои комнаты") })
    }, floatingActionButton = {
        FloatingActionButton(onClick = { /* TODO: Создать комнату */ }) {
            Icon(Icons.Default.Add, "Создать комнату")
        }
    }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // TODO: Список комнат
            Text("Здесь будет список ваших комнат", modifier = Modifier.padding(16.dp))

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}