package com.example.zapas.core.utils

data class PagingPage<T>(
    val data: List<T>,
    val prevPage: Int?,
    val nextPage: Int?
)
