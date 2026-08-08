package com.example.zapas.data.api.dto.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RegisterRequestDto(
    @Json(name = "bio") val bio: String,
    @Json(name = "display_name") val displayName: String,
    @Json(name = "email") val email: String? = null,
    @Json(name = "passwd") val password: String,
    @Json(name = "username") val username: String
)
