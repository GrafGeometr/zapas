package com.example.zapas.domain.repository

import arrow.core.Either
import com.example.zapas.domain.models.AppError

interface AuthRepository {
    suspend fun login(username: String, password: String): Either<AppError, Unit>
    suspend fun register(
        username: String,
        password: String,
        displayName: String,
        bio: String
    ): Either<AppError, Unit>

    suspend fun prolong(): Either<AppError, Unit>
    suspend fun logout()
    suspend fun isLoggedIn(): Boolean
}