package com.example.zapas.features.rooms.data.dto.response

import com.example.zapas.core.network.dto.ResponseDto
import com.example.zapas.features.rooms.domain.model.MembersPage
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MembersDto(
    @Json(name = "data") val data: List<MemberDto>,
    @Json(name = "next_page") val nextPage: Int?,
    @Json(name = "prev_page") val prevPage: Int?,
    @Json(name = "pag") val pag: Pag
) : ResponseDto<MembersPage> {
    override fun toDomain() = MembersPage(
        members = data.map { it.toDomain() }, prevPage = prevPage, nextPage = nextPage
    )
}

@JsonClass(generateAdapter = true)
data class Pag(
    @Json(name = "page") val page: Int?,
    @Json(name = "reverse") val reverse: Boolean?,
    @Json(name = "size") val size: Int?
)
