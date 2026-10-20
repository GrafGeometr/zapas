package com.example.zapas.core.utils

import androidx.paging.PagingSource
import androidx.paging.PagingState
import arrow.core.Either
import com.example.zapas.core.domain.model.AppError

open class DefaultPagingSource<T : Any>(
    private val getter: suspend (LoadParams<Int>) -> Either<AppError, PagingPage<T>>
) : PagingSource<Int, T>() {
    override fun getRefreshKey(state: PagingState<Int, T>): Int? =
        state.anchorPosition?.let { position ->
            state.closestPageToPosition(position)?.let { page ->
                page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
            }
        }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val result = getter(params)

        return result.fold({ err: AppError ->
            LoadResult.Error(err)
        }, {
            LoadResult.Page(
                data = it.data, prevKey = it.prevPage, nextKey = it.nextPage
            )
        })
    }
}