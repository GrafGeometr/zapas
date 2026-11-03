package com.example.zapas.core.network

import arrow.core.Either
import com.example.zapas.core.domain.model.AppError
import com.example.zapas.core.local.TokenManager
import com.example.zapas.features.auth.domain.model.Token
import com.example.zapas.features.auth.domain.repository.AuthRepository
import dagger.Lazy
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.IOException

class TokenAuthenticatorTest {
    private val tokens = mockk<TokenManager>()
    private val repository = mockk<AuthRepository>()
    private val authenticator = TokenAuthenticator(tokens, Lazy { repository })
    private val response = Response.Builder()
        .request(Request.Builder().url("https://example.test/").header("Authorization", "Bearer token").build())
        .protocol(Protocol.HTTP_1_1).code(401).message("Unauthorized").build()

    @Test
    fun `network failure and server error preserve the token`() {
        coEvery { tokens.getBasicToken() } returns Token.Basic("token")
        for (error in listOf(AppError.NetworkError(IOException()), AppError.ApiError(0, "Unavailable", 503))) {
            coEvery { repository.prolong() } returns Either.Left(error)
            assertNull(authenticator.authenticate(null, response))
        }
        coVerify(exactly = 0) { tokens.clearTokens(any()) }
    }

    @Test
    fun `rejected refresh invalidates the session`() {
        coEvery { tokens.getBasicToken() } returns Token.Basic("token")
        coEvery { repository.prolong() } returns Either.Left(AppError.ApiError(0, "Invalid token", 401))
        coEvery { tokens.clearTokens(any()) } returns Unit
        assertNull(authenticator.authenticate(null, response))
        coVerify(exactly = 1) { tokens.clearTokens(sessionExpired = true) }
    }
}
