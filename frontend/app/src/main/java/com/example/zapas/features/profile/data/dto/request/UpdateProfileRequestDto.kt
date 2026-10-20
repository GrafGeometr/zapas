package com.example.zapas.features.profile.data.dto.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UpdateProfileRequestDto(
    @Json(name = "display_name") val displayName: String?,
    @Json(name = "bio") val bio: String?,
    @Json(name = "username") val username: String?
)