package com.example.zapas.data.api.dto.response

import com.squareup.moshi.Json

data class LoginResponseDto(
    @Json(name = "token") val refreshToken: String
)
