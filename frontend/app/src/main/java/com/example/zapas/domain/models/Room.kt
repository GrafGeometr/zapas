package com.example.zapas.domain.models

data class Room(
    val bio: String,
    val createdAt: String,
    val displayName: String,
    val id: String,
    val expiredNotify: String?,
    val reviewInterval: String?
)