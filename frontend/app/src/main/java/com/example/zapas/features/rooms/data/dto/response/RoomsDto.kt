package com.example.zapas.features.rooms.data.dto.response

import com.example.zapas.core.network.dto.ResponseDto
import com.example.zapas.features.rooms.domain.model.RoomsPage
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RoomsDto(
    @Json(name = "data") val data: List<RoomProfileDto>,
    @Json(name = "next_page") val nextPage: Int?,
    @Json(name = "prev_page") val prevPage: Int?,
    @Json(name = "pag") val pag: Pag
) : ResponseDto<RoomsPage> {
    override fun toDomain() = RoomsPage(
        rooms = data.map { it.toDomain() },
        prevPage = prevPage,
        nextPage = nextPage
    )
}

