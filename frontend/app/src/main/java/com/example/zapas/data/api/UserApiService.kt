package com.example.zapas.data.api

import com.example.zapas.data.api.dto.request.ChangePasswordRequestDto
import com.example.zapas.data.api.dto.request.UpdateProfileRequestDto
import com.example.zapas.data.api.dto.response.UserMeDto
import com.example.zapas.data.api.dto.response.UuidDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface UserApiService {

    @GET("user/me")
    suspend fun me(): Response<UserMeDto>

    @POST("user/me/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequestDto): Response<UserMeDto>

    @POST("user/me/passwd")
    suspend fun updatePassword(@Body request: ChangePasswordRequestDto): Response<Unit>

    @POST("user/delete")
    suspend fun deleteUser(@Body request: UuidDto): Response<Unit>

    @POST("user/me/logout")
    suspend fun logout(): Response<Unit>
}
