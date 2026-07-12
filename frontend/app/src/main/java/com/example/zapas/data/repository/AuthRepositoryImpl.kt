package com.example.zapas.data.repository

import com.example.zapas.data.api.AuthApiService
import com.example.zapas.data.api.dto.request.AuthAccessRequestDto
import com.example.zapas.data.api.dto.request.AuthRefreshRequestDto
import com.example.zapas.data.api.dto.request.LoginRequestDto
import com.example.zapas.data.api.dto.request.RegisterRequestDto
import com.example.zapas.data.api.dto.request.UserLoginDto
import com.example.zapas.data.local.TokenManager
import com.example.zapas.domain.models.Token
import com.example.zapas.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApiService, private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun login(username: String, password: String): Boolean {
        try {
            val response = authApi.login(
                request = LoginRequestDto(
                    username,
                    password
                )
            )

            if (response.isSuccessful) {
                val tokenDto = response.body()

                if (tokenDto != null) {
                    tokenManager.saveToken(Token.Refresh(tokenDto.refreshToken))
                    return true
                }
            }

            return false

        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    override suspend fun register(
        username: String,
        password: String,
        displayName: String
    ): Boolean {
        try {
            val requestDto = RegisterRequestDto(
                login = UserLoginDto(
                    username,
                    password
                ),
                displayName
            )

            val response = authApi.register(request = requestDto)

            if (response.isSuccessful) {
                return login(username, password)
            }

            return false

        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    override suspend fun refreshRefreshToken(refreshToken: String): Boolean {
        try {
            val request = AuthRefreshRequestDto(refreshToken)
            val response = authApi.refresh(request)

            if (response.isSuccessful) {
                val tokenDto = response.body()
                if (tokenDto != null) {
                    tokenManager.saveToken(Token.Refresh(tokenDto.refreshToken))
                    return true
                }
            }

            return false
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    override suspend fun refreshAccessToken(refreshToken: String): Boolean {
        try {
            val request = AuthAccessRequestDto(refreshToken)
            val response = authApi.authAccess(request)

            if (response.isSuccessful) {
                val tokenDto = response.body()
                if (tokenDto != null) {
                    tokenManager.saveToken(Token.Access(tokenDto.accessToken))
                    return true
                }
            }

            return false
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    override suspend fun isLoggedIn(): Boolean {
        return tokenManager.hasToken()
    }

    override suspend fun logout() {
        tokenManager.clearTokens()
    }
}