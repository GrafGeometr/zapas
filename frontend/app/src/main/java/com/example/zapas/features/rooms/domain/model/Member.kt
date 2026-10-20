package com.example.zapas.features.rooms.domain.model

data class Member(
    val bio: String,
    val createdAt: String,
    val displayName: String,
    val id: String,
    val roomId: String,
    val userId: String?
)