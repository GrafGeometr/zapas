package com.example.zapas.data.api.dto.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AppErrJson(
    @Json(name = "code") val code: Int,
    @Json(name = "msg") val msg: String
)
