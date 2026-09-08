package com.example.zapas.data.api.dto.response

import com.example.zapas.domain.models.User
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserMeDto(
    @Json(name = "id") val id: String,
    @Json(name = "username") val username: String,
    @Json(name = "verified_email") val verifiedEmail: Boolean,
    @Json(name = "display_name") val displayName: String,
    @Json(name = "bio") val bio: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "email") val email: String? = null
) : ResponseDto<User> {
    override fun toDomain(): User = User(
        id = id,
        username = username,
        displayName = displayName,
        bio = bio,
        email = email,
        verifiedEmail = verifiedEmail,
        createdAt = createdAt
    )
}
