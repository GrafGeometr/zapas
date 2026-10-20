package com.example.zapas.features.rooms.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.example.zapas.core.utils.PagingSourceList
import com.example.zapas.features.rooms.domain.model.Room

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
