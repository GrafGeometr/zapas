package com.example.zapas.features.rooms.data.dto.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateRoomDto(
    @Json(name = "bio") val bio: String,
    @Json(name = "display_name") val displayName: String
)