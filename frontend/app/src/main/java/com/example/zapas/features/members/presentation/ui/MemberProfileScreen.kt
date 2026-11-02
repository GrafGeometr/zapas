package com.example.zapas.features.members.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.zapas.features.members.presentation.viewmodel.MemberProfileState
import com.example.zapas.features.members.presentation.viewmodel.MemberProfileViewModel

@Composable
fun MemberProfileScreen(
    onBack: () -> Unit,
    viewModel: MemberProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    MemberProfileScreenContent(
        state, onBack, viewModel::load, viewModel::changeMonth,
        viewModel::openProfileEditor, viewModel::changeDefaultPortions, viewModel::changePortion
    )
    if (state.showProfileDialog)
        MemberProfileDialog(state, viewModel::dismissProfileEditor, viewModel::saveProfile)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberProfileScreenContent(
    state: MemberProfileState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onChangeMonth: (Int) -> Unit = {},
    onEditProfile: () -> Unit = {},
    onChangeDefaultPortions: (Int) -> Unit = {},
    onChangePortion: (String, Int) -> Unit = { _, _ -> }
) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Участник комнаты") },
            navigationIcon = {
                IconButton(onClick = onBack, enabled = !state.isSaving) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                }
            },
            actions = {
                IconButton(onClick = onRefresh, enabled = !state.isSaving) {
                    Icon(Icons.Default.Refresh, contentDescription = "Обновить")
                }
            }
        )
    }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                if (state.isLoading || state.isSaving) CircularProgressIndicator()
                if (!state.showProfileDialog) state.error?.let { MemberError(it, onRefresh) }
            }
            state.member?.let { member ->
                item {
                    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(member.displayName.ifBlank { "Без имени" }, style = MaterialTheme.typography.headlineSmall)
                            if (state.isOwnProfile == true)
                                Text("Это вы", color = MaterialTheme.colorScheme.primary)
                            if (member.bio.isNotBlank()) Text(member.bio)
                            if (state.isOwnProfile == true)
                                TextButton(
                                    onClick = onEditProfile,
                                    enabled = state.canEdit && state.localProfile != null
                                ) { Text("Редактировать профиль в комнате") }
                        }
                    }
                }
                item {
                    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Порций по умолчанию", style = MaterialTheme.typography.titleMedium)
                            state.defaultPortions?.let { count ->
                                MemberPortionCounter(count, state.isOwnProfile == true, state.canEdit, onChangeDefaultPortions)
                            }
                        }
                    }
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(onClick = { onChangeMonth(-1) }, enabled = !state.isSaving) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Предыдущий месяц")
                            }
                            Text(state.periodTitle, style = MaterialTheme.typography.titleMedium)
                            IconButton(onClick = { onChangeMonth(1) }, enabled = !state.isSaving) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Следующий месяц")
                            }
                        }
                        Text("Порции", style = MaterialTheme.typography.titleMedium)
                        if (!state.isLoading && state.hasLoadedPortions && state.portions.isEmpty())
                            Text("За этот период записей нет")
                    }
                }
                items(state.portions) { record ->
                    Column {
                        ListItem(
                            headlineContent = { Text(record.date) },
                            trailingContent = {
                                MemberPortionCounter(record.portions, state.isOwnProfile == true, state.canEdit) {
                                    onChangePortion(record.date, it)
                                }
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberError(error: String, onRetry: () -> Unit) {
    Column {
        Text(error, color = MaterialTheme.colorScheme.error)
        TextButton(onClick = onRetry) { Text("Повторить") }
    }
}

@Composable
fun MemberPortionCounter(
    count: Long,
    showControls: Boolean,
    enabled: Boolean,
    onChange: (Int) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (showControls)
            IconButton(
                onClick = { onChange(-1) },
                enabled = enabled && count > 0,
                modifier = Modifier.semantics { contentDescription = "Уменьшить порции на 1" }
            ) {
                Text("−", style = MaterialTheme.typography.titleLarge)
            }
        Text(count.toString())
        if (showControls)
            IconButton(onClick = { onChange(1) }, enabled = enabled && count >= 0 && count < Long.MAX_VALUE) {
                Icon(Icons.Default.Add, contentDescription = "Увеличить порции на 1")
            }
    }
}
