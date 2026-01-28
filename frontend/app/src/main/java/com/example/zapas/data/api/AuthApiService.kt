package com.example.zapas.data.api


import com.example.zapas.data.api.dto.request.LoginRequestDto
import com.example.zapas.data.api.dto.request.RegisterRequestDto
import com.example.zapas.data.api.dto.response.TokenResponseDto
import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {

    @POST("auth/login/universal")
    suspend fun login(@Body request: LoginRequestDto): Response<TokenResponseDto>

    @POST("auth/reg")
    suspend fun register(@Body request: RegisterRequestDto): Response<TokenResponseDto>

    @POST("auth/refresh/universal")
    suspend fun refreshTokens(@Header("Authorization") authHeader: String): Response<TokenResponseDto>

}
