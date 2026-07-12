package com.example.zapas.data.local


import com.example.zapas.data.local.storage.KeyValueStorage
import com.example.zapas.domain.models.Token

class TokenManager(
    private val storage: KeyValueStorage
) {
    suspend fun <T : Token> saveToken(token: T) {
        val (field, value) = when (token) {
            is Token.Access -> "access_token" to token.value
            is Token.Refresh -> "refresh_token" to token.value
            is Token.Admin -> "admin_token" to token.value
            else -> throw Exception()
        }
        val gotAtField = field + "_got_at"

        storage.saveString(field, value)
        storage.saveLong(gotAtField, System.currentTimeMillis())
    }

    suspend fun getAccessToken(): Token.Access? {
        val token = storage.getString("access_token")
        return token?.let { Token.Access(it) }
    }

    suspend fun getRefreshToken(): Token.Refresh? {
        val token = storage.getString("refresh_token")
        return token?.let { Token.Refresh(it) }
    }

    suspend fun getAuthHeader(): String? {
        val token = getAccessToken()
        return token?.let { "Bearer ${it.value}" }
    }

    suspend fun clearTokens() {
        storage.remove("access_token")
        storage.remove("refresh_token")
        storage.remove("admin_token")

        storage.remove("access_token_got_at")
        storage.remove("refresh_token_got_at")
        storage.remove("admin_token_got_at")
    }

    suspend fun hasToken(): Boolean {
        return storage.getString("refresh_token") != null
    }
}