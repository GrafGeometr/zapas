package com.example.zapas.data.api

import arrow.core.Either
import com.example.zapas.data.local.TokenManager
import com.example.zapas.domain.repository.AuthRepository
import dagger.Lazy
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager, private val authRepository: Lazy<AuthRepository>
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        if (shouldSkipAuth(originalRequest)) {
            return chain.proceed(originalRequest)
        }

        val requestWithToken = originalRequest.withBasicToken() ?: originalRequest

        val response = chain.proceed(requestWithToken)

        if (response.code == 401 && !originalRequest.url.encodedPath.contains("/user/")) {
            response.close()

            val success = runBlocking { authRepository.get().prolong() is Either.Right }

            if (success) {
                val newRequest = originalRequest.withBasicToken()

                if (newRequest != null) {
                    return chain.proceed(newRequest)
                }
            }

            runBlocking { tokenManager.clearTokens() }
        }

        return response
    }

    private fun Request.withBasicToken() = runBlocking { tokenManager.getBasicToken() }?.let {
        newBuilder().header("Authorization", "Bearer ${it.value}").build()
    }

    private fun shouldSkipAuth(request: Request): Boolean {
        val path = request.url.encodedPath
        return path.contains("/user/login") || path.contains("/user/register") || path.contains("/user/prolong")
    }
}
