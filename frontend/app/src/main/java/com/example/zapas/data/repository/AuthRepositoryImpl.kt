package com.example.zapas.data.repository

import arrow.core.Either
import arrow.core.flatMap
import com.example.zapas.data.api.AuthApiService
import com.example.zapas.data.api.dto.request.LoginRequestDto
import com.example.zapas.data.api.dto.request.RegisterRequestDto
import com.example.zapas.data.api.util.safeApiCall
import com.example.zapas.data.local.TokenManager
import com.example.zapas.domain.models.AppError
import com.example.zapas.domain.models.Token
import com.example.zapas.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApiService, private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun login(username: String, password: String): Either<AppError, Unit> {
        return safeApiCall {
            authApi.login(LoginRequestDto(username, password))
        }.map { tokenDto ->
            tokenManager.saveToken(Token.Basic(tokenDto.token))
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

        return safeApiCall {
            authApi.register(requestDto)
        }.flatMap {
            login(username, password)
        }
    }

    override suspend fun prolong(): Either<AppError, Unit> {
        return safeApiCall {
            authApi.prolong()
        }.map { tokenDto ->
            tokenManager.saveToken(Token.Basic(tokenDto.token))
        }
    }

    override suspend fun isLoggedIn(): Boolean {
        return tokenManager.hasToken()
    }
}
