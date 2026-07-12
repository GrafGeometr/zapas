package com.example.zapas.data.api.dto.request

import com.squareup.moshi.Json

data class RegisterRequestDto(
    @Json(name = "login") val login: UserLoginDto,
    @Json(name = "display_name") val displayName: String
)

data class UserLoginDto(
    @Json(name = "username") val username: String,
    @Json(name = "passwd") val password: String
)
