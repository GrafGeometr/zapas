package com.example.zapas.ui.presentation.screens.rooms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.zapas.domain.models.Room
import com.example.zapas.ui.presentation.components.PreviewableAlertDialog
import com.example.zapas.ui.presentation.utils.PagingSourceList
import com.example.zapas.ui.presentation.viewmodels.rooms.RoomsPagingSourceList
import com.example.zapas.ui.presentation.viewmodels.rooms.RoomsUiState
import com.example.zapas.ui.presentation.viewmodels.rooms.RoomsViewModel
import com.example.zapas.ui.theme.ZapasTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomsScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToCreateRoom: () -> Unit,
    onNavigateToRoom: (String) -> Unit = {},
    viewModel: RoomsViewModel = hiltViewModel()
) {
    val pagingItems = viewModel.rooms.collectAsLazyPagingItems()
    val uiState by viewModel.uiState.collectAsState()

    val items = remember(pagingItems) { RoomsPagingSourceList(pagingItems) }

    RoomScreenContent(
        items = items,
        uiState = uiState,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToCreateRoom = onNavigateToCreateRoom,
        onNavigateToRoom = onNavigateToRoom,
        onShowDialog = { viewModel.setShowJoinDialog(true) },
        onDismissDialog = { viewModel.setShowJoinDialog(false) },
        onSetInviteToken = viewModel::setInviteTokenInput,
        onJoinRoom = { token -> viewModel.joinRoom(token, onNavigateToRoom) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomScreenContent(
    items: PagingSourceList<Room>,
    uiState: RoomsUiState,
    onNavigateToProfile: () -> Unit,
    onNavigateToCreateRoom: () -> Unit,
    onNavigateToRoom: (String) -> Unit = {},
    onShowDialog: () -> Unit = {},
    onDismissDialog: () -> Unit = {},
    onSetInviteToken: (String) -> Unit = {},
    onJoinRoom: (String) -> Unit = {}
) {
    Scaffold(topBar = {
        TopAppBar(title = { Text("Мои комнаты") }, actions = {
            IconButton(onClick = onNavigateToProfile) {
                Icon(Icons.Default.Person, contentDescription = "Профиль")
            }
        })
    }, floatingActionButton = {
        FloatingActionButton(onClick = onShowDialog) {
            Icon(Icons.Default.Add, "Присоединиться к комнате")
        }
    }) { paddingValues ->
        if (uiState.showJoinDialog) {
            JoinRoomDialog(
                uiState = uiState,
                onDismiss = onDismissDialog,
                onSetInviteToken = onSetInviteToken,
                onJoinRoom = onJoinRoom,
                onCreateRoomClick = {
                    onDismissDialog()
                    onNavigateToCreateRoom()
                }
            )
        }

        if (items.itemCount == 0 && !items.isRefreshing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "У вас пока нет комнат",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(
                    count = items.itemCount
                ) { index ->

                    val room = items[index]

                    if (room != null) {
                        RoomItem(
                            room = room,
                            onClick = { onNavigateToRoom(room.id) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }

                if (items.isLoadingMore) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

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

@Composable
fun RoomItem(
    room: Room,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        modifier = modifier.clickable(onClick = onClick),
        headlineContent = {
            Text(
                text = room.displayName,
                style = MaterialTheme.typography.titleMedium
            )
        },
        supportingContent = if (room.bio.isNotBlank()) {
            {
                Text(
                    text = room.bio,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else null,
        leadingContent = {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}

class RoomsPagingSourceListMock(
    private val rooms: List<Room>
) : PagingSourceList<Room> {
    override val itemCount: Int
        get() = rooms.size

    override fun get(index: Int): Room? {
        return rooms.getOrNull(index)
    }

    override val isRefreshing: Boolean = false

    override val isLoadingMore: Boolean = false

    override val refreshError: Throwable? = null

    override val appendError: Throwable? = null

    override fun retry() = Unit
}

@Preview
@Composable
fun RoomScreenPreview() {
    RoomScreenContent(
        items = RoomsPagingSourceListMock(
        (1..100).map {
            Room(
                bio = "bio",
                createdAt = "2006",
                displayName = "Room${it}",
                id = it.toString(),
                expiredNotify = "Whatever",
                reviewInterval = "Something"
            )
        }),
        uiState = RoomsUiState(),
        onNavigateToProfile = {},
        onNavigateToCreateRoom = {}
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
