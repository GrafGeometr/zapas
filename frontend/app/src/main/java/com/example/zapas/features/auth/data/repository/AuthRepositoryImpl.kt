package com.example.zapas.features.auth.data.repository

import arrow.core.Either
import arrow.core.flatMap
import com.example.zapas.core.network.NetworkErrorMapper
import com.example.zapas.core.local.TokenManager
import com.example.zapas.core.domain.model.AppError
import com.example.zapas.features.auth.domain.repository.AuthRepository
import com.example.zapas.features.auth.data.api.AuthApiService
import com.example.zapas.features.auth.data.dto.request.LoginRequestDto
import com.example.zapas.features.auth.data.dto.request.RegisterRequestDto
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