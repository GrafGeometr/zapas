package com.example.zapas.data.api.dto.response

data class RegisterResponseDto(
    val created_at: String,
    val id: String,
    val profile: RegisterResponseProfileDto,
    val username: String
)

data class RegisterResponseProfileDto(
    val bio: String,
    val display_name: String
)