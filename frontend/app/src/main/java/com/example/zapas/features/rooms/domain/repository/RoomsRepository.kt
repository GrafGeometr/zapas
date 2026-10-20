package com.example.zapas.features.rooms.domain.repository

import androidx.paging.PagingData
import arrow.core.Either
import com.example.zapas.core.domain.model.AppError
import com.example.zapas.features.rooms.data.dto.response.MemberDto
import com.example.zapas.features.rooms.domain.model.Member
import com.example.zapas.features.rooms.domain.model.MembersPage
import com.example.zapas.features.rooms.domain.model.Room
import com.example.zapas.features.rooms.domain.model.RoomsPage
import kotlinx.coroutines.flow.Flow

interface RoomsRepository {
    suspend fun getRoomsList(page: Int?, size: Int): Either<AppError, RoomsPage>

    fun getRoomsPaged(): Flow<PagingData<Room>>

    suspend fun createRoom(bio: String, displayName: String): Either<AppError, String>

    suspend fun getRoomProfile(roomId: String): Either<AppError, Room>

    suspend fun editRoomProfile(roomId: String, displayName: String?, bio: String?): Either<AppError, Room>

    suspend fun updateRoomTimings(roomId: String, expiredNotify: String?, reviewInterval: String?): Either<AppError, Room>

    suspend fun getInviteToken(roomId: String): Either<AppError, String>

    suspend fun joinRoom(token: String): Either<AppError, String>

    suspend fun getRoomMembers(roomId: String, page: Int? = 0, size: Int = 50): Either<AppError, MembersPage>

    fun getMembersPaged(roomId: String): Flow<PagingData<Member>>

    suspend fun deleteRoom(roomId: String): Either<AppError, Unit>
}