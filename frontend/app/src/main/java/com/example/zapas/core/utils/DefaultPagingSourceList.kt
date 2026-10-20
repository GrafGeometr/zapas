package com.example.zapas.core.utils

import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems

class DefaultPagingSourceList<T : Any>(
    private val items: LazyPagingItems<T>
) : PagingSourceList<T> {
    override val itemCount: Int
        get() = items.itemCount

    override fun get(index: Int): T? = items[index]

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
