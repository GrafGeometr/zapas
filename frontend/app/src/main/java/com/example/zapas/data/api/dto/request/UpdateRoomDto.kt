package com.example.zapas.data.api.dto.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UpdateRoomDto(
    @Json(name = "bio") val bio: String?,
    @Json(name = "display_name") val displayName: String?
)