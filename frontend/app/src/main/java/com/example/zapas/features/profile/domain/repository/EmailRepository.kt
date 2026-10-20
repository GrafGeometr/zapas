package com.example.zapas.features.profile.domain.repository

import arrow.core.Either
import com.example.zapas.core.domain.model.AppError

interface EmailRepository {
    suspend fun resetPassword(email: String): Either<AppError, Unit>

    suspend fun updateEmail(email: String): Either<AppError, Unit>

    suspend fun verifyEmail(): Either<AppError, Unit>
}