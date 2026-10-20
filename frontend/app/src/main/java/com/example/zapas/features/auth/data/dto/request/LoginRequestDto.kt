package com.example.zapas.features.auth.data.dto.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequestDto(
    @Json(name = "username") val username: String,
    @Json(name = "passwd") val password: String
)