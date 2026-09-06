package com.example.zapas.data.api.services


import com.example.zapas.data.api.dto.request.LoginRequestDto
import com.example.zapas.data.api.dto.request.RegisterRequestDto
import com.example.zapas.data.api.dto.response.TokenDto
import com.example.zapas.data.api.dto.response.UuidDto
import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {

    @POST("user/register")
    suspend fun register(@Body request: RegisterRequestDto): Response<UuidDto>

    @POST("user/login")
    suspend fun login(@Body request: LoginRequestDto): Response<TokenDto>

    @POST("user/prolong")
    suspend fun prolong(): Response<TokenDto>
}
