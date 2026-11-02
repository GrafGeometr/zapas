package com.example.zapas.features.members.presentation.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.zapas.features.members.domain.model.MemberPortion
import com.example.zapas.features.members.presentation.viewmodel.MemberDialog
import com.example.zapas.features.members.presentation.viewmodel.MemberProfileState
import com.example.zapas.features.members.presentation.viewmodel.MemberProfileViewModel
import com.example.zapas.features.rooms.domain.model.Member
import com.example.zapas.ui.theme.ZapasTheme

@Composable
fun MemberProfileScreen(
    onBack: () -> Unit,
    viewModel: MemberProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    MemberProfileScreenContent(
        state, onBack, viewModel::load, viewModel::changeMonth,
        viewModel::openDialog
    )
    when (state.dialog) {
        MemberDialog.PROFILE -> MemberProfileDialog(state, viewModel::dismissDialog, viewModel::saveProfile)
        MemberDialog.DEFAULT_PORTIONS, MemberDialog.PORTION -> MemberPortionsDialog(
            state, viewModel::dismissDialog, viewModel::savePortions
        )
        null -> Unit
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberProfileScreenContent(
    state: MemberProfileState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onChangeMonth: (Int) -> Unit = {},
    onOpenDialog: (MemberDialog, MemberPortion?) -> Unit = { _, _ -> }
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
                if (state.isLoading) CircularProgressIndicator()
                if (state.dialog == null) state.error?.let { MemberError(it, onRefresh) }
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
                                    onClick = { onOpenDialog(MemberDialog.PROFILE, null) },
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
                            state.defaultPortions?.let { Text(it.toString()) }
                            if (state.isOwnProfile == true)
                                TextButton(
                                    onClick = { onOpenDialog(MemberDialog.DEFAULT_PORTIONS, null) },
                                    enabled = state.canEdit && state.defaultPortions != null
                                ) { Text("Изменить") }
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
                            modifier = Modifier.clickable(enabled = state.canEdit, role = Role.Button) {
                                onOpenDialog(MemberDialog.PORTION, record)
                            },
                            headlineContent = { Text(record.date) },
                            trailingContent = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(record.portions.toString())
                                    if (state.isOwnProfile == true)
                                        Icon(Icons.Default.Edit, contentDescription = "Изменить порции")
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

@Preview(showBackground = true)
@Composable
private fun MemberProfilePreview() {
    ZapasTheme {
        MemberProfileScreenContent(
            state = MemberProfileState(
                member = Member("Описание в комнате", "2026-11-01T00:00:00Z", "Алексей", "member", "room", "user"),
                currentUserId = "user",
                defaultPortions = 1,
                portions = listOf(MemberPortion("2026-11-01", 0), MemberPortion("2026-11-02", 2)),
                periodTitle = "ноябрь 2026",
                hasLoadedPortions = true,
                isLoading = false
            ),
            onBack = {},
            onRefresh = {}
        )
    }
}
