package com.example.zapas.ui.presentation.screens.rooms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.zapas.domain.models.Room
import com.example.zapas.ui.presentation.utils.PagingSourceList
import com.example.zapas.ui.presentation.viewmodels.rooms.RoomsPagingSourceList
import com.example.zapas.ui.presentation.viewmodels.rooms.RoomsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomsScreen(
    onNavigateToProfile: () -> Unit, viewModel: RoomsViewModel = hiltViewModel()
) {
    val pagingItems = viewModel.rooms.collectAsLazyPagingItems()

    val items = remember(pagingItems) { RoomsPagingSourceList(pagingItems) }

    RoomScreenContent(items = items, onNavigateToProfile)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomScreenContent(
    items: PagingSourceList<Room>, onNavigateToProfile: () -> Unit
) {
    Scaffold(topBar = {
        TopAppBar(title = { Text("Мои комнаты") }, actions = {
            IconButton(onClick = onNavigateToProfile) {
                Icon(Icons.Default.Person, contentDescription = "Профиль")
            }
        })
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
            LazyColumn {
                items(
                    count = items.itemCount
                ) { index ->

                    val room = items[index]

                    if (room != null) {
                        Text(room.id)
                    }
                }

                if (items.isLoadingMore) {
                    item {
                        CircularProgressIndicator()
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
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
        }), onNavigateToProfile = {})
}
