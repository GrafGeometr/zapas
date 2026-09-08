package com.example.zapas.data.repository

import arrow.core.Either
import arrow.core.flatMap
import com.example.zapas.data.api.services.AuthApiService
import com.example.zapas.data.api.dto.request.LoginRequestDto
import com.example.zapas.data.api.dto.request.RegisterRequestDto
import com.example.zapas.data.api.util.NetworkErrorMapper
import com.example.zapas.data.local.TokenManager
import com.example.zapas.domain.models.AppError
import com.example.zapas.domain.models.Token
import com.example.zapas.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApiService,
    private val tokenManager: TokenManager,
    private val networkErrorMapper: NetworkErrorMapper
) : AuthRepository {

    override suspend fun login(username: String, password: String): Either<AppError, Unit> {
        return networkErrorMapper.safeApiCallMapped {
            authApi.login(LoginRequestDto(username, password))
        }.map { token ->
            tokenManager.saveToken(token)
        }
    }

    override suspend fun register(
        username: String,
        password: String,
        displayName: String,
        bio: String
    ): Either<AppError, Unit> {
        val requestDto = RegisterRequestDto(
            username = username,
            displayName = displayName,
            bio = bio,
            password = password
        )

        return networkErrorMapper.safeApiCall {
            authApi.register(requestDto)
        }.flatMap {
            login(username, password)
        }
    }

    override suspend fun prolong(): Either<AppError, Unit> {
        return networkErrorMapper.safeApiCallMapped {
            authApi.prolong()
        }.map { token ->
            tokenManager.saveToken(token)
        }
    }

    override suspend fun isLoggedIn(): Boolean {
        return tokenManager.hasToken()
    }
}
