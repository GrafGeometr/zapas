package com.example.zapas.data.api.dto.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChangePasswordRequestDto(
    @Json(name = "old_passwd") val oldPasswd: String,
    @Json(name = "passwd") val newPasswd: String
)
