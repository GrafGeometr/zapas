package com.example.zapas.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import arrow.core.Either
import com.example.zapas.data.api.dto.request.CreateRoomDto
import com.example.zapas.data.api.dto.request.UpdateRoomDto
import com.example.zapas.data.api.dto.request.UpdateRoomTimingsDto
import com.example.zapas.data.api.dto.response.MemberDto
import com.example.zapas.data.api.services.RoomsApiService
import com.example.zapas.data.api.util.NetworkErrorMapper
import com.example.zapas.data.repository.paging.RoomsPagingSource
import com.example.zapas.domain.models.AppError
import com.example.zapas.domain.models.Room
import com.example.zapas.domain.repository.RoomsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RoomsRepositoryImpl @Inject constructor(
    private val roomApi: RoomsApiService, private val networkErrorMapper: NetworkErrorMapper
) : RoomsRepository {
    override suspend fun getRoomsList(
        key: Int?, size: Int
    ) = networkErrorMapper.safeApiCallMapped { roomApi.rooms(key, size) }

    override fun getRoomsPaged(): Flow<PagingData<Room>> = Pager(
        config = PagingConfig(
            pageSize = 100, prefetchDistance = 20, initialLoadSize = 100
        ), pagingSourceFactory = {
            RoomsPagingSource(this)
        }).flow

    override suspend fun createRoom(bio: String, displayName: String): Either<AppError, String> =
        networkErrorMapper.safeApiCall { roomApi.createRoom(CreateRoomDto(bio, displayName)) }
            .map { it.id }

    override suspend fun joinRoom(token: String): Either<AppError, String> =
        networkErrorMapper.safeApiCall { roomApi.invite(token) }
            .map { it.id }

    override suspend fun getRoomProfile(roomId: String): Either<AppError, Room> =
        networkErrorMapper.safeApiCallMapped { roomApi.room(roomId) }

    override suspend fun editRoomProfile(
        roomId: String,
        displayName: String?,
        bio: String?
    ): Either<AppError, Room> =
        networkErrorMapper.safeApiCallMapped {
            roomApi.editRoom(roomId, UpdateRoomDto(bio = bio, displayName = displayName))
        }

    override suspend fun updateRoomTimings(
        roomId: String,
        expiredNotify: String?,
        reviewInterval: String?
    ): Either<AppError, Room> =
        networkErrorMapper.safeApiCallMapped {
            roomApi.updateTimings(
                roomId,
                UpdateRoomTimingsDto(expiredNotify = expiredNotify, reviewInterval = reviewInterval)
            )
        }

    override suspend fun getInviteToken(roomId: String): Either<AppError, String> =
        networkErrorMapper.safeApiCall { roomApi.getInviteToken(roomId) }
            .map { it.token }

    override suspend fun getRoomMembers(
        roomId: String,
        num: Int,
        size: Int
    ): Either<AppError, List<MemberDto>> =
        networkErrorMapper.safeApiCall { roomApi.members(roomId, num, size) }

    override suspend fun deleteRoom(roomId: String): Either<AppError, Unit> =
        networkErrorMapper.safeApiCall { roomApi.deleteRoom(roomId) }
}