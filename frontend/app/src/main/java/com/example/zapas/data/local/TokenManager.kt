package com.example.zapas.data.local


import com.example.zapas.data.local.storage.KeyValueStorage
import com.example.zapas.domain.models.Token

class TokenManager(
    private val storage: KeyValueStorage
) {
    suspend fun <T : Token> saveToken(token: T) {
        val (field, value) = when (token) {
            is Token.Verify -> "verify_token" to token.value
            is Token.Refresh -> "refresh_token" to token.value
            is Token.Invite -> "invite_token" to token.value
            is Token.Admin -> "admin_token" to token.value
            is Token.ForRoom -> "for_room_token" to token.value
            else -> throw Exception()
        }
        val got_at_field = field + "_got_at"

        storage.saveString(field, value)
        storage.saveLong(got_at_field, System.currentTimeMillis())
    }

    // Получаем токен по типу
    suspend fun getVerifyToken(): Token.Verify? {
        val token = storage.getString("verify_token")
        return token?.let { Token.Verify(it) }
    }

    suspend fun getRefreshToken(): Token.Refresh? {
        val token = storage.getString("refresh_token")
        return token?.let { Token.Refresh(it) }
    }

    // Получаем токен для заголовка Authorization
    suspend fun getAuthHeader(): String? {
        val token = storage.getString("verify_token")
        return token?.let { "Bearer $it" }
    }

    // Удаляем все токены (для логаута)
    suspend fun clearTokens() {
        storage.remove("verify_token")
        storage.remove("refresh_token")
        storage.remove("invite_token")
        storage.remove("admin_token")
        storage.remove("for_room_token")

        // Удаляем временные метки
        storage.remove("verify_token_got_at")
        storage.remove("refresh_token_got_at")
        storage.remove("invite_token_got_at")
        storage.remove("admin_token_got_at")
        storage.remove("for_room_token_got_at")
    }

    // Проверяем, есть ли токен
    suspend fun hasToken(): Boolean {
        return storage.getString("verify_token") != null
    }
}