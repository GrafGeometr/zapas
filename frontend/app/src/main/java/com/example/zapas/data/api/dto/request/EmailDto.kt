package com.example.zapas.data.api.dto.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EmailDto(
    @Json(name = "email") val email: String
)
