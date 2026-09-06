package com.example.zapas.data.api.services

import com.example.zapas.data.api.dto.request.CreateRoomDto
import com.example.zapas.data.api.dto.request.UpdateRoomDto
import com.example.zapas.data.api.dto.request.UpdateRoomTimingsDto
import com.example.zapas.data.api.dto.response.MembersDto
import com.example.zapas.data.api.dto.response.RoomProfileDto
import com.example.zapas.data.api.dto.response.RoomsDto
import com.example.zapas.data.api.dto.response.TokenDto
import com.example.zapas.data.api.dto.response.UuidDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RoomsApiService {
    @GET("room/all")
    suspend fun rooms(@Query("num") num: Int?, @Query("size") size: Int): Response<RoomsDto>

    @POST("room/invite/{token}")
    suspend fun invite(@Path("token") tokenString: String): Response<UuidDto>

    @POST("room/make")
    suspend fun createRoom(@Body request: CreateRoomDto): Response<UuidDto>

    @GET("room/{room_id}")
    suspend fun room(@Path("room_id") roomId: String): Response<RoomProfileDto>

    @POST("room/{room_id}/delete")
    suspend fun deleteRoom(@Path("room_id") roomId: String): Response<Unit>

    @POST("room/{room_id}/invite/gen")
    suspend fun getInviteToken(@Path("room_id") roomId: String): Response<TokenDto>

    @GET("room/{room_id}/members")
    suspend fun members(
        @Path("room_id") roomId: String,
        @Query("num") num: Int,
        @Query("size") size: Int
    ): Response<MembersDto>

    @POST("room/{room_id}/profile")
    suspend fun editRoom(
        @Path("room_id") roomId: String,
        @Body request: UpdateRoomDto
    ): Response<RoomProfileDto>

    @POST("room/{room_id}/timing")
    suspend fun updateTimings(
        @Path("room_id") roomId: String,
        @Body request: UpdateRoomTimingsDto
    ): Response<RoomProfileDto>

}