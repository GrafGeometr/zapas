package com.example.zapas.features.profile.domain.repository

import arrow.core.Either
import com.example.zapas.core.domain.model.AppError
import com.example.zapas.features.profile.domain.model.User

interface UserRepository {
    suspend fun getMe(): Either<AppError, User>
    suspend fun updateProfile(displayName: String?, bio: String?, username: String?): Either<AppError, User>
    suspend fun changePassword(oldPasswd: String, newPasswd: String): Either<AppError, Unit>
    suspend fun deleteUser(userId: String): Either<AppError, Unit>
    suspend fun logout()
}