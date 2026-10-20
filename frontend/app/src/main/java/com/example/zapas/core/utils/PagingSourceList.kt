package com.example.zapas.core.utils

interface PagingSourceList<T> {
    val itemCount: Int

    operator fun get(index: Int): T?

    val isRefreshing: Boolean
    val isLoadingMore: Boolean
    val refreshError: Throwable?
    val appendError: Throwable?

    fun retry()
}
