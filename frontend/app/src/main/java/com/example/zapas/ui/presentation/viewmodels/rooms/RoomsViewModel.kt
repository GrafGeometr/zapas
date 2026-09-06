package com.example.zapas.ui.presentation.viewmodels.rooms

import androidx.lifecycle.ViewModel
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.zapas.data.api.services.RoomsApiService
import javax.inject.Inject
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.compose.LazyPagingItems
import com.example.zapas.domain.models.Room
import com.example.zapas.ui.presentation.utils.PagingSourceList
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import com.example.zapas.data.api.dto.response.RoomsDto
import com.example.zapas.data.api.dto.response.RoomProfileDto
import dagger.hilt.android.lifecycle.HiltViewModel

class RoomsPagingSource(
    private val roomApi: RoomsApiService
) : PagingSource<Int, Room>() {
    override fun getRefreshKey(state: PagingState<Int, Room>) =
        state.anchorPosition?.let { position ->
            state.closestPageToPosition(position)?.let { page ->
                page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
            }
        }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Room> = try {
        val key = params.key

        val response = roomApi.rooms(key, params.loadSize)

        if (!response.isSuccessful) {
            LoadResult.Error(HttpException(response))
        } else {

            val result = response.body()!!

            val rooms = result.data.map {
                Room(
                    bio = it.bio,
                    createdAt = it.createdAt,
                    displayName = it.displayName,
                    id = it.id,
                    expiredNotify = it.expiredNotify,
                    reviewInterval = it.reviewInterval
                )
            }



            LoadResult.Page(
                data = rooms,

                prevKey = result.prevPage,

                nextKey = result.nextPage
            )
        }
    } catch (e: Exception) {
        LoadResult.Error(e)
    }
}


@HiltViewModel
class RoomsViewModel @Inject constructor(
    private val roomApi: RoomsApiService
) : ViewModel() {
    private var activePagingSource: RoomsPagingSource? = null

    val rooms: Flow<PagingData<Room>> = Pager(
        config = PagingConfig(
            pageSize = 20, prefetchDistance = 5
        ), pagingSourceFactory = {
            RoomsPagingSource(roomApi).also { source ->
                activePagingSource = source
            }
        }).flow
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



