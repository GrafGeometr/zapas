package com.example.zapas.domain.models

data class RoomsPage(
    val rooms: List<Room>,
    val prevPage: Int?,
    val nextPage: Int?
)
