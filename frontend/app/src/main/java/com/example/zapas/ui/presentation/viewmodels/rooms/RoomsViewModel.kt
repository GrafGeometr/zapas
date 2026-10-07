package com.example.zapas.ui.presentation.viewmodels.rooms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.compose.LazyPagingItems
import com.example.zapas.domain.models.Room
import com.example.zapas.domain.usecase.rooms.GetRoomsPagedUseCase
import com.example.zapas.ui.presentation.utils.PagingSourceList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


@HiltViewModel
class RoomsViewModel @Inject constructor(
    private val getRoomsPagedUseCase: GetRoomsPagedUseCase
) : ViewModel() {
    val rooms: Flow<PagingData<Room>> = getRoomsPagedUseCase().cachedIn(viewModelScope)
}

class RoomsPagingSourceList(
    private val items: LazyPagingItems<Room>
) : PagingSourceList<Room> {
    override val itemCount: Int
        get() = items.itemCount

    override fun get(index: Int): Room? = items[index]

    override val isRefreshing: Boolean
        get() = items.loadState.refresh is LoadState.Loading
    override val isLoadingMore: Boolean
        get() = items.loadState.append is LoadState.Loading
    override val refreshError: Throwable?
        get() = (items.loadState.refresh as? LoadState.Error)?.error
    override val appendError: Throwable?
        get() = (items.loadState.append as? LoadState.Error)?.error

    override fun retry() {
        items.retry()
    }
}
