package com.example.zapas.data.api.dto.response

import com.squareup.moshi.Json

data class RegisterResponseDto(
    @Json(name = "created_at") val createdAt: String,
    val id: String,
    val profile: RegisterResponseProfileDto,
    val username: String
)

data class RegisterResponseProfileDto(
    val bio: String, @Json(name = "display_name") val displayName: String
)