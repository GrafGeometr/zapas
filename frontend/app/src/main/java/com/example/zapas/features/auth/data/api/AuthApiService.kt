package com.example.zapas.features.auth.data.api

import com.example.zapas.features.auth.data.dto.request.LoginRequestDto
import com.example.zapas.features.auth.data.dto.request.RegisterRequestDto
import com.example.zapas.features.auth.data.dto.response.TokenDto
import com.example.zapas.core.network.dto.UuidDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("user/register")
    suspend fun register(@Body request: RegisterRequestDto): Response<UuidDto>

    @POST("user/login")
    suspend fun login(@Body request: LoginRequestDto): Response<TokenDto>

    @POST("user/prolong")
    suspend fun prolong(): Response<TokenDto>
}