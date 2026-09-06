package com.example.zapas.data.api.services

import com.example.zapas.data.api.dto.request.EmailDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface EmailApiService {
    @POST("user/forgot")
    suspend fun forgotPassword(@Body request: EmailDto): Response<Unit>

    @POST("me/email")
    suspend fun updateEmail(@Body request: EmailDto): Response<Unit>

    @POST("me/email/verify")
    suspend fun verifyEmail(): Response<Unit>
}