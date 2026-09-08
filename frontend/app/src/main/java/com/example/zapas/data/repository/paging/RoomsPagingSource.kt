package com.example.zapas.data.repository.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.zapas.domain.models.AppError
import com.example.zapas.domain.models.Room
import com.example.zapas.domain.models.RoomsPage
import com.example.zapas.domain.repository.RoomsRepository

class RoomsPagingSource(
    private val roomsRepository: RoomsRepository
) : PagingSource<Int, Room>() {
    override fun getRefreshKey(state: PagingState<Int, Room>): Int? =
        state.anchorPosition?.let { position ->
            state.closestPageToPosition(position)?.let { page ->
                page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
            }
        }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Room> {
        val key = params.key

        val result = roomsRepository.getRoomsList(key, params.loadSize)

        return result.fold({ err: AppError ->
            LoadResult.Error(err)
        }, { it: RoomsPage ->
            LoadResult.Page(
                data = it.rooms, prevKey = it.prevPage, nextKey = it.nextPage
            )
        })
    }
}

