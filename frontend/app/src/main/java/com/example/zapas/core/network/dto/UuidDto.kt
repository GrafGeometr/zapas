package com.example.zapas.core.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UuidDto(
    @Json(name = "id") val id: String
)
