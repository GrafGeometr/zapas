package com.example.zapas.features.auth.domain.repository

import arrow.core.Either
import com.example.zapas.core.domain.model.AppError

interface AuthRepository {
    suspend fun login(username: String, password: String): Either<AppError, Unit>
    suspend fun register(
        username: String,
        password: String,
        displayName: String,
        bio: String
    ): Either<AppError, Unit>

    suspend fun prolong(): Either<AppError, Unit>
    suspend fun isLoggedIn(): Boolean
}