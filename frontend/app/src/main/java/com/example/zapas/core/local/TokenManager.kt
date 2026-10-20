package com.example.zapas.core.local

import com.example.zapas.core.local.storage.KeyValueStorage
import com.example.zapas.features.auth.domain.model.Token

class TokenManager(
    private val storage: KeyValueStorage
) {
    suspend fun <T : Token> saveToken(token: T) {
        val (field, value) = when (token) {
            is Token.Basic -> "basic_token" to token.value
        }
        val gotAtField = field + "_got_at"

        storage.saveString(field, value)
        storage.saveLong(gotAtField, System.currentTimeMillis())
    }

    suspend fun getBasicToken(): Token.Basic? {
        val token = storage.getString("basic_token")
        return token?.let { Token.Basic(it) }
    }

    suspend fun getAuthHeader(): String? {
        val token = getBasicToken()
        return token?.let { "Bearer ${it.value}" }
    }

    suspend fun clearTokens() {
        storage.remove("basic_token")
        storage.remove("basic_token_got_at")
    }

    suspend fun hasToken(): Boolean {
        return storage.getString("basic_token") != null
    }
}
