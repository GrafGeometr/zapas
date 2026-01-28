package com.example.zapas.data.repository

import com.example.zapas.data.api.AuthApiService
import com.example.zapas.data.api.dto.request.LoginRequestDto
import com.example.zapas.data.api.dto.request.RegisterRequestDto
import com.example.zapas.data.api.dto.request.UserLoginDto
import com.example.zapas.data.api.dto.response.TokenResponseDto
import com.example.zapas.data.local.TokenManager
import com.example.zapas.domain.models.Token
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authApi: AuthApiService, private val tokenManager: TokenManager
) {

    suspend fun login(username: String, password: String): Boolean {
        try {
            // 1. Создаём DTO для запроса
            val request = LoginRequestDto(
                username = username,
                passwd = password
            )

            // 2. Отправляем запрос на сервер
            val response = authApi.login(request)

            // 3. Проверяем успешность
            if (response.isSuccessful) {
                val tokenDto: TokenResponseDto? = response.body()

                // 4. Сохраняем токен
                if (tokenDto != null) {
                    // Предполагаю, что это verify токен
                    tokenManager.saveToken(Token.Verify(tokenDto.token))
                    return true
                }
            }

            return false

        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    suspend fun register(username: String, password: String, display_name: String): Boolean {
        try {
            // 1. Создаём DTO для регистрации
            val request = RegisterRequestDto(
                login = UserLoginDto(
                    username = username,
                    passwd = password
                ),
                display_name = display_name
            )

            // 2. Отправляем запрос
            val response = authApi.register(request)

            // 3. Если успешно - сразу логинимся
            if (response.isSuccessful) {
                return login(username, password)
            }

            return false

        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    suspend fun refreshToken(): Boolean {
        try {
            // 1. Получаем refresh токен
            val refreshToken = tokenManager.getRefreshToken()
            if (refreshToken == null) {
                return false
            }

            // 2. Отправляем запрос на обновление
            val authHeader = "Bearer ${refreshToken.value}"
            val response = authApi.refreshTokens(authHeader)

            // 3. Сохраняем новый токен
            if (response.isSuccessful) {
                val tokenDto = response.body()
                if (tokenDto != null) {
                    tokenManager.saveToken(Token.Verify(tokenDto.token))
                    return true
                }
            }

            return false

        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    suspend fun isLoggedIn(): Boolean {
        return tokenManager.hasToken()
    }

    suspend fun logout() {
        tokenManager.clearTokens()
    }
}

