package com.example.zapas.data.api.dto.response

import com.squareup.moshi.Json

data class AuthAdminResponseDto(
    @Json(name = "token") val adminToken: String
)
