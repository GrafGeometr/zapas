package com.example.zapas.features.rooms.data.dto.response

import com.example.zapas.core.network.dto.ResponseDto
import com.example.zapas.features.rooms.domain.model.Member
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MemberDto(
    @Json(name = "bio") val bio: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "display_name") val displayName: String,
    @Json(name = "id") val id: String,
    @Json(name = "room_id") val roomId: String,
    @Json(name = "user_id") val userId: String?
) : ResponseDto<Member> {
    override fun toDomain() = Member(
        bio, createdAt, displayName, id, roomId, userId
    )
}