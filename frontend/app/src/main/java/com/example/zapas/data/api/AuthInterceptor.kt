package com.example.zapas.data.api


import com.example.zapas.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Не добавляем токен к запросам на аутентификацию и регистрацию
        if (shouldSkipAuth(originalRequest)) {
            return chain.proceed(originalRequest)
        }

        // Получаем токен (в Interceptor'е используем runBlocking)
        val token = runBlocking {
            tokenManager.getVerifyToken()
        }

        // Если токен есть, добавляем его в заголовок
        val requestWithToken = if (!token.isNullOrEmpty()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(requestWithToken)
    }

    private fun shouldSkipAuth(request: Request): Boolean {
        val path = request.url.encodedPath
        return path.contains("/auth/") && !path.contains("/auth/refresh")
    }
}