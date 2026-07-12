package com.example.zapas.data.api


import com.example.zapas.data.api.dto.request.AuthAccessRequestDto
import com.example.zapas.data.api.dto.request.AuthRefreshRequestDto
import com.example.zapas.data.api.dto.request.LoginRequestDto
import com.example.zapas.data.api.dto.request.RegisterRequestDto
import com.example.zapas.data.api.dto.response.AuthAccessResponseDto
import com.example.zapas.data.api.dto.response.AuthAdminResponseDto
import com.example.zapas.data.api.dto.response.AuthRefreshResponseDto
import com.example.zapas.data.api.dto.response.LoginResponseDto
import com.example.zapas.data.api.dto.response.RegisterResponseDto
import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {
    @POST("auth/access")
    suspend fun authAccess(@Body request: AuthAccessRequestDto): Response<AuthAccessResponseDto>

    @POST("auth/admin")
    suspend fun authAdmin(@Body request: LoginRequestDto): Response<AuthAdminResponseDto>

    @POST("auth/create")
    suspend fun register(@Body request: RegisterRequestDto): Response<RegisterResponseDto>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<LoginResponseDto>


    @POST("auth/refresh")
    suspend fun refresh(@Body request: AuthRefreshRequestDto): Response<AuthRefreshResponseDto>

}
