package com.example.zapas.core.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AppErrJson(
    @Json(name = "code") val code: Int,
    @Json(name = "msg") val msg: String
)
