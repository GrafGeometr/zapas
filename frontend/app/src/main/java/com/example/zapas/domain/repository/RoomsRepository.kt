package com.example.zapas.domain.repository

import androidx.paging.PagingData
import arrow.core.Either
import com.example.zapas.data.api.dto.response.MemberDto
import com.example.zapas.domain.models.AppError
import com.example.zapas.domain.models.Room
import com.example.zapas.domain.models.RoomsPage
import kotlinx.coroutines.flow.Flow

interface RoomsRepository {
    suspend fun getRoomsList(key: Int?, size: Int): Either<AppError, RoomsPage>

    fun getRoomsPaged(): Flow<PagingData<Room>>

    suspend fun createRoom(bio: String, displayName: String): Either<AppError, String>

    suspend fun getRoomProfile(roomId: String): Either<AppError, Room>

    suspend fun editRoomProfile(roomId: String, displayName: String?, bio: String?): Either<AppError, Room>

    suspend fun updateRoomTimings(roomId: String, expiredNotify: String?, reviewInterval: String?): Either<AppError, Room>

    suspend fun getInviteToken(roomId: String): Either<AppError, String>

    suspend fun joinRoom(token: String): Either<AppError, String>

    suspend fun getRoomMembers(roomId: String, num: Int = 0, size: Int = 50): Either<AppError, List<MemberDto>>

    suspend fun deleteRoom(roomId: String): Either<AppError, Unit>
}