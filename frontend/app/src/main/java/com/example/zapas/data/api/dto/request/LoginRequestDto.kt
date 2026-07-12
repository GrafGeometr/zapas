package com.example.zapas.data.api.dto.request

import com.squareup.moshi.Json


data class LoginRequestDto(
    @Json(name = "username") val username: String,
    @Json(name = "passwd") val password: String
)
