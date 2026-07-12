package com.example.zapas.data.api.dto.response

import com.squareup.moshi.Json

data class AuthRefreshResponseDto(
    @Json(name = "token") val refreshToken: String
)
