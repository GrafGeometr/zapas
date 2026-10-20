package com.example.zapas.features.rooms.data.dto.response

import com.example.zapas.core.network.dto.ResponseDto
import com.example.zapas.core.utils.PagingPage
import com.example.zapas.features.rooms.domain.model.Member
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MembersDto(
    @Json(name = "data") val data: List<MemberDto>,
    @Json(name = "next_page") val nextPage: Int?,
    @Json(name = "prev_page") val prevPage: Int?,
    @Json(name = "pag") val pag: Pag
) : ResponseDto<PagingPage<Member>> {
    override fun toDomain() = PagingPage(
        data = data.map { it.toDomain() }, prevPage = prevPage, nextPage = nextPage
    )
}

@JsonClass(generateAdapter = true)
data class Pag(
    @Json(name = "page") val page: Int?,
    @Json(name = "reverse") val reverse: Boolean?,
    @Json(name = "size") val size: Int?
)
