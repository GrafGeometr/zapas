package com.example.zapas.data.api.dto.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class RoomProfileDto (
    @Json(name = "bio") val bio: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "display_name") val displayName: String,
    @Json(name = "id") val id: String,
    @Json(name = "expired_notify") val expiredNotify: String?,
    @Json(name = "review_interval") val reviewInterval: String?
)