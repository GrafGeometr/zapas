package com.example.zapas.data.api.dto.request

import com.squareup.moshi.Json

data class AuthAccessRequestDto(
    @Json(name = "token") val refreshToken: String
)
