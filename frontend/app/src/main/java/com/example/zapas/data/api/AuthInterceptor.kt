package com.example.zapas.data.api

import com.example.zapas.data.api.dto.request.AuthAccessRequestDto
import com.example.zapas.data.local.TokenManager
import com.example.zapas.domain.models.Token
import com.example.zapas.domain.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager, private val authRepository: AuthRepository
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        if (shouldSkipAuth(originalRequest)) {
            return chain.proceed(originalRequest)
        }

        val accessToken = runBlocking { tokenManager.getAccessToken() }
        val requestWithToken = if (accessToken != null) {
            originalRequest.newBuilder().header("Authorization", "Bearer ${accessToken.value}")
                .build()
        } else {
            originalRequest
        }

        val response = chain.proceed(requestWithToken)

        if (response.code == 401 && !originalRequest.url.encodedPath.contains("/auth/")) {
            response.close()

            val newAccessToken = runBlocking { refreshAccessToken() }

            if (newAccessToken != null) {
                val newRequest =
                    originalRequest.newBuilder().header("Authorization", "Bearer $newAccessToken")
                        .build()
                return chain.proceed(newRequest)
            } else {
                runBlocking { tokenManager.clearTokens() }
            }
        }

        return response
    }

    private suspend fun refreshAccessToken(): String? {
        val refreshToken = tokenManager.getRefreshToken() ?: return null

        try {
            val refreshSuccess =
                authRepository.refreshRefreshToken(refreshToken = refreshToken.value)
            if (!refreshSuccess) return null

            val newRefreshToken = tokenManager.getRefreshToken() ?: return null
            authRepository.refreshAccessToken(refreshToken = newRefreshToken.value)

            return tokenManager.getAccessToken()?.value

        } catch (e: Exception) {
            return null
        }
    }

    private fun shouldSkipAuth(request: Request): Boolean {
        val path = request.url.encodedPath
        return path.contains("/auth/login") || path.contains("/auth/create") || path.contains("/auth/access") || path.contains(
            "/auth/refresh"
        )
    }
}