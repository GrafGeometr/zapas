package com.example.zapas.features.profile.domain.model

data class User(
    val id: String,
    val username: String,
    val displayName: String,
    val bio: String,
    val email: String?,
    val verifiedEmail: Boolean,
    val createdAt: String
)