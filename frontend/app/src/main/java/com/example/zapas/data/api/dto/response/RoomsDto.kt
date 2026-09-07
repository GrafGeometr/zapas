package com.example.zapas.data.api.dto.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RoomsDto(
    @Json(name = "data") val data: List<RoomProfileDto>,
    @Json(name = "next_page") val nextPage: Int?,
    @Json(name = "prev_page") val prevPage: Int?,
    @Json(name = "pag") val pag: Pag
)

@JsonClass(generateAdapter = true)
data class Pag(
    @Json(name = "page") val page: Int?,
    @Json(name = "reverse") val reverse: Boolean?,
    @Json(name = "size") val size: Int?
)
