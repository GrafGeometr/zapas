package com.example.zapas.domain.models

data class User(
    val id: String,
    val username: String,
    val displayName: String,
    val bio: String,
    val email: String?,
    val verifiedEmail: Boolean,
    val createdAt: String
)
