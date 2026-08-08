package com.example.zapas.data.repository

import arrow.core.Either
import com.example.zapas.data.api.UserApiService
import com.example.zapas.data.api.dto.request.ChangePasswordRequestDto
import com.example.zapas.data.api.dto.request.UpdateProfileRequestDto
import com.example.zapas.data.api.dto.response.UserMeDto
import com.example.zapas.data.api.dto.response.UuidDto
import com.example.zapas.data.api.util.safeApiCall
import com.example.zapas.data.local.TokenManager
import com.example.zapas.domain.models.AppError
import com.example.zapas.domain.models.User
import com.example.zapas.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApiService,
    private val tokenManager: TokenManager
) : UserRepository {

    override suspend fun getMe(): Either<AppError, User> {
        return safeApiCall { userApi.me() }.map { it.toDomain() }
    }

    override suspend fun updateProfile(
        displayName: String?,
        bio: String?,
        username: String?
    ): Either<AppError, User> {
        return safeApiCall {
            userApi.updateProfile(
                UpdateProfileRequestDto(
                    displayName = displayName,
                    bio = bio,
                    username = username
                )
            )
        }.map { it.toDomain() }
    }

    override suspend fun changePassword(oldPasswd: String, newPasswd: String): Either<AppError, Unit> {
        return safeApiCall {
            userApi.updatePassword(ChangePasswordRequestDto(oldPasswd, newPasswd))
        }
    }

    override suspend fun deleteUser(userId: String): Either<AppError, Unit> {
        return safeApiCall {
            userApi.deleteUser(UuidDto(userId))
        }
    }

    override suspend fun logout() {
        try {
            userApi.logout()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        tokenManager.clearTokens()
    }

    private fun UserMeDto.toDomain(): User {
        return User(
            id = id,
            username = username,
            displayName = displayName,
            bio = bio,
            email = email,
            verifiedEmail = verifiedEmail,
            createdAt = createdAt
        )
    }
}
