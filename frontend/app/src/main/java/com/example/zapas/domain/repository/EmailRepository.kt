package com.example.zapas.domain.repository

import arrow.core.Either
import com.example.zapas.domain.models.AppError

interface EmailRepository {
    suspend fun resetPassword(email: String): Either<AppError, Unit>

    suspend fun updateEmail(email: String): Either<AppError, Unit>

    suspend fun verifyEmail(): Either<AppError, Unit>
}