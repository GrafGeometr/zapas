package com.example.zapas.domain.repository


interface AuthRepository {
    suspend fun login(username: String, password: String): Boolean
    suspend fun register(username: String, password: String, displayName: String): Boolean
    suspend fun refreshRefreshToken(refreshToken: String): Boolean
    suspend fun refreshAccessToken(refreshToken: String): Boolean
    suspend fun logout()
    suspend fun isLoggedIn(): Boolean
}