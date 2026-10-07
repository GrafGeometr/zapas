package com.example.zapas.ui.presentation.screens.rooms

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import com.example.zapas.ui.presentation.components.PreviewableAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.zapas.data.api.dto.response.MemberDto
import com.example.zapas.domain.models.Room
import com.example.zapas.ui.presentation.viewmodels.rooms.RoomProfileState
import com.example.zapas.ui.presentation.viewmodels.rooms.RoomProfileViewModel
import com.example.zapas.ui.theme.ZapasTheme

@Composable
fun RoomProfileScreen(
    onBack: () -> Unit,
    viewModel: RoomProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) {
            onBack()
        }
    }

    LaunchedEffect(state.actionSuccessMessage, state.actionError) {
        state.actionSuccessMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearActionMessages()
        }
        state.actionError?.let { err ->
            snackbarHostState.showSnackbar(err)
            viewModel.clearActionMessages()
        }
    }

    RoomProfileScreenContent(
        state = state,
        onBack = onBack,
        onRefresh = viewModel::load,
        onShowEditDialog = { viewModel.setShowEditDialog(true) },
        onShowTimingsDialog = { viewModel.setShowTimingsDialog(true) },
        onGenerateInvite = viewModel::generateInviteToken,
        onShowDeleteDialog = { viewModel.setShowDeleteDialog(true) }
    )

    // Dialogs
    if (state.showEditDialog && state.room != null) {
        EditRoomProfileDialog(
            currentName = state.room?.displayName.orEmpty(),
            currentBio = state.room?.bio.orEmpty(),
            isLoading = state.isActionLoading,
            onDismiss = { viewModel.setShowEditDialog(false) },
            onConfirm = viewModel::editProfile
        )
    }

    if (state.showTimingsDialog && state.room != null) {
        UpdateTimingsDialog(
            currentExpiredNotify = state.room?.expiredNotify.orEmpty(),
            currentReviewInterval = state.room?.reviewInterval.orEmpty(),
            isLoading = state.isActionLoading,
            onDismiss = { viewModel.setShowTimingsDialog(false) },
            onConfirm = viewModel::updateTimings
        )
    }

    if (state.showInviteDialog) {
        InviteTokenDialog(
            token = state.inviteToken.orEmpty(),
            onDismiss = { viewModel.setShowInviteDialog(false) }
        )
    }

    if (state.showDeleteDialog) {
        DeleteRoomDialog(
            isLoading = state.isActionLoading,
            onDismiss = { viewModel.setShowDeleteDialog(false) },
            onConfirm = viewModel::deleteRoom
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomProfileScreenContent(
    state: RoomProfileState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onShowEditDialog: () -> Unit,
    onShowTimingsDialog: () -> Unit,
    onGenerateInvite: () -> Unit,
    onShowDeleteDialog: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль комнаты") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Обновить")
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Еще")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Редактировать") },
                                onClick = {
                                    showMenu = false
                                    onShowEditDialog()
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Настройки времени") },
                                onClick = {
                                    showMenu = false
                                    onShowTimingsDialog()
                                },
                                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Код приглашения") },
                                onClick = {
                                    showMenu = false
                                    onGenerateInvite()
                                },
                                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Удалить комнату",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onShowDeleteDialog()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading && state.room == null -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                state.error != null && state.room == null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = state.error,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Button(onClick = onRefresh) {
                            Text("Повторить")
                        }
                    }
                }

                state.room != null -> {
                    val room = state.room
                    val clipboardManager = LocalClipboardManager.current
                    val context = LocalContext.current

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Room Header Card
                        RoomHeaderCard(
                            room = room,
                            onCopyId = {
                                clipboardManager.setText(AnnotatedString(room.id))
                                Toast.makeText(context, "ID комнаты скопирован", Toast.LENGTH_SHORT).show()
                            }
                        )

                        // Quick Actions Bar
                        QuickActionsBar(
                            onGenerateInvite = onGenerateInvite,
                            onEdit = onShowEditDialog,
                            onTimings = onShowTimingsDialog
                        )

                        // Room Bio / Description Card
                        RoomBioCard(bio = room.bio)

                        // Room Settings / Timings Card
                        RoomTimingsCard(
                            expiredNotify = room.expiredNotify,
                            reviewInterval = room.reviewInterval,
                            onEditTimings = onShowTimingsDialog
                        )

                        // Room Members Section
                        RoomMembersSection(
                            members = state.members,
                            isLoading = state.isMembersLoading
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Delete Room Button
                        OutlinedButton(
                            onClick = onShowDeleteDialog,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Удалить комнату")
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun RoomHeaderCard(
    room: Room,
    onCopyId: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = room.displayName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Room ID Pill
            Surface(
                onClick = onCopyId,
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "ID: ${room.id}",
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Копировать ID",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (room.createdAt.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "Создана: ${room.createdAt}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionsBar(
    onGenerateInvite: () -> Unit,
    onEdit: () -> Unit,
    onTimings: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onGenerateInvite,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Инвайт", style = MaterialTheme.typography.labelLarge)
        }

        OutlinedButton(
            onClick = onEdit,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Инфо", style = MaterialTheme.typography.labelLarge)
        }

        OutlinedButton(
            onClick = onTimings,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Тайминги", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun RoomBioCard(bio: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Описание",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (bio.isNotBlank()) bio else "Описание комнаты не указано",
                style = MaterialTheme.typography.bodyMedium,
                color = if (bio.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun RoomTimingsCard(
    expiredNotify: String?,
    reviewInterval: String?,
    onEditTimings: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Параметры уведомлений",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onEditTimings, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Редактировать тайминги",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Column {
                    Text(
                        text = "Уведомление об истечении срока",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = expiredNotify.takeIf { !it.isNullOrBlank() } ?: "Не установлено",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Column {
                    Text(
                        text = "Интервал инвентаризации / проверки",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = reviewInterval.takeIf { !it.isNullOrBlank() } ?: "Не установлено",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun RoomMembersSection(
    members: List<MemberDto>,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Участники (${members.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            }
        }

        if (members.isEmpty() && !isLoading) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Text(
                    text = "Список участников пуст",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    members.forEachIndexed { index, member ->
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = member.displayName.ifBlank { "Без имени" },
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            supportingContent = if (member.bio.isNotBlank()) {
                                { Text(member.bio, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                            } else null,
                            leadingContent = {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        )
                        if (index < members.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditRoomProfileDialog(
    currentName: String,
    currentBio: String,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var bio by remember { mutableStateOf(currentBio) }

    PreviewableAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактировать комнату") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название комнаты") },
                    singleLine = true,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Описание") },
                    minLines = 3,
                    maxLines = 5,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, bio) },
                enabled = !isLoading && name.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Сохранить")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun UpdateTimingsDialog(
    currentExpiredNotify: String,
    currentReviewInterval: String,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var expiredNotify by remember { mutableStateOf(currentExpiredNotify) }
    var reviewInterval by remember { mutableStateOf(currentReviewInterval) }

    PreviewableAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Настройки времени") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = expiredNotify,
                    onValueChange = { expiredNotify = it },
                    label = { Text("Уведомление об истечении") },
                    placeholder = { Text("Например: 24h, 7d") },
                    singleLine = true,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = reviewInterval,
                    onValueChange = { reviewInterval = it },
                    label = { Text("Интервал проверки") },
                    placeholder = { Text("Например: 30d") },
                    singleLine = true,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(expiredNotify, reviewInterval) },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Сохранить")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun InviteTokenDialog(
    token: String,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    PreviewableAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Приглашение в комнату") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Передайте этот токен новому участнику для входа в комнату:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = token,
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    clipboardManager.setText(AnnotatedString(token))
                    Toast.makeText(context, "Токен скопирован в буфер обмена", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Скопировать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        }
    )
}

@Composable
fun DeleteRoomDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    PreviewableAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Удалить комнату?") },
        text = { Text("Вы уверены, что хотите удалить эту комнату? Это действие нельзя отменить.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onError,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Удалить")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Отмена")
            }
        }
    )
}

// --- Previews ---

private val sampleRoom = Room(
    id = "room-1234-5678",
    displayName = "Кладовка #1",
    bio = "Главный склад запасов и инструментов на даче.",
    createdAt = "2026-01-15",
    expiredNotify = "24h",
    reviewInterval = "30d"
)

private val sampleMembers = listOf(
    MemberDto(
        id = "m1",
        roomId = "room-1234-5678",
        userId = "u1",
        displayName = "Алексей Иванов",
        bio = "Администратор",
        createdAt = "2026-01-15"
    ),
    MemberDto(
        id = "m2",
        roomId = "room-1234-5678",
        userId = "u2",
        displayName = "Мария Петрова",
        bio = "Участник",
        createdAt = "2026-02-01"
    )
)

@Preview(showBackground = true, name = "Успешная загрузка")
@Composable
fun RoomProfileScreenSuccessPreview() {
    ZapasTheme {
        RoomProfileScreenContent(
            state = RoomProfileState(
                room = sampleRoom,
                members = sampleMembers,
                isLoading = false
            ),
            onBack = {},
            onRefresh = {},
            onShowEditDialog = {},
            onShowTimingsDialog = {},
            onGenerateInvite = {},
            onShowDeleteDialog = {}
        )
    }
}

@Preview(showBackground = true, name = "Без участников")
@Composable
fun RoomProfileScreenEmptyMembersPreview() {
    ZapasTheme {
        RoomProfileScreenContent(
            state = RoomProfileState(
                room = sampleRoom,
                members = emptyList(),
                isLoading = false
            ),
            onBack = {},
            onRefresh = {},
            onShowEditDialog = {},
            onShowTimingsDialog = {},
            onGenerateInvite = {},
            onShowDeleteDialog = {}
        )
    }
}

@Preview(showBackground = true, name = "Загрузка")
@Composable
fun RoomProfileScreenLoadingPreview() {
    ZapasTheme {
        RoomProfileScreenContent(
            state = RoomProfileState(
                isLoading = true,
                room = null
            ),
            onBack = {},
            onRefresh = {},
            onShowEditDialog = {},
            onShowTimingsDialog = {},
            onGenerateInvite = {},
            onShowDeleteDialog = {}
        )
    }
}

@Preview(showBackground = true, name = "Ошибка загрузки")
@Composable
fun RoomProfileScreenErrorPreview() {
    ZapasTheme {
        RoomProfileScreenContent(
            state = RoomProfileState(
                isLoading = false,
                error = "Не удалось загрузить данные комнаты. Проверьте подключение к сети.",
                room = null
            ),
            onBack = {},
            onRefresh = {},
            onShowEditDialog = {},
            onShowTimingsDialog = {},
            onGenerateInvite = {},
            onShowDeleteDialog = {}
        )
    }
}

@Preview(showBackground = true, name = "Тёмная тема", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun RoomProfileScreenDarkPreview() {
    ZapasTheme {
        RoomProfileScreenContent(
            state = RoomProfileState(
                room = sampleRoom,
                members = sampleMembers,
                isLoading = false
            ),
            onBack = {},
            onRefresh = {},
            onShowEditDialog = {},
            onShowTimingsDialog = {},
            onGenerateInvite = {},
            onShowDeleteDialog = {}
        )
    }
}

@Preview(showBackground = true, name = "Диалог - Редактирование")
@Composable
fun EditRoomProfileDialogPreview() {
    ZapasTheme {
        Surface {
            EditRoomProfileDialog(
                currentName = sampleRoom.displayName,
                currentBio = sampleRoom.bio,
                isLoading = false,
                onDismiss = {},
                onConfirm = { _, _ -> }
            )
        }
    }
}

@Preview(showBackground = true, name = "Диалог - Тайминги")
@Composable
fun UpdateTimingsDialogPreview() {
    ZapasTheme {
        UpdateTimingsDialog(
            currentExpiredNotify = sampleRoom.expiredNotify.orEmpty(),
            currentReviewInterval = sampleRoom.reviewInterval.orEmpty(),
            isLoading = false,
            onDismiss = {},
            onConfirm = { _, _ -> }
        )
    }
}

@Preview(showBackground = true, name = "Диалог - Инвайт токен")
@Composable
fun InviteTokenDialogPreview() {
    ZapasTheme {
        InviteTokenDialog(
            token = "ABC123XYZ789",
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true, name = "Диалог - Удаление комнаты")
@Composable
fun DeleteRoomDialogPreview() {
    ZapasTheme {
        DeleteRoomDialog(
            isLoading = false,
            onDismiss = {},
            onConfirm = {}
        )
    }
}
