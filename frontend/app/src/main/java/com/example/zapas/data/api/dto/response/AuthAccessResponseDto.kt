package com.example.zapas.data.api.dto.response

import com.squareup.moshi.Json

data class AuthAccessResponseDto(
    @Json(name = "token") val accessToken: String
)
