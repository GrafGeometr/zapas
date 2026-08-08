package com.example.zapas.data.api


import com.example.zapas.data.api.dto.request.LoginRequestDto
import com.example.zapas.data.api.dto.request.RegisterRequestDto
import com.example.zapas.data.api.dto.response.TokenDto
import com.example.zapas.data.api.dto.response.UserMeDto
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

    @GET("user/me")
    suspend fun me(): Response<UserMeDto>

    @POST("user/me/logout")
    suspend fun logout(): Response<Unit>
}
