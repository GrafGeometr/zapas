package com.example.zapas.domain.repository

import androidx.paging.PagingData
import arrow.core.Either
import com.example.zapas.data.api.dto.response.UuidDto
import com.example.zapas.domain.models.AppError
import com.example.zapas.domain.models.Room
import com.example.zapas.domain.models.RoomsPage
import kotlinx.coroutines.flow.Flow

interface RoomsRepository {
    suspend fun getRoomsList(key: Int?, size: Int): Either<AppError, RoomsPage>

    fun getRoomsPaged(): Flow<PagingData<Room>>

    suspend fun createRoom(bio: String, displayName: String): Either<AppError, UuidDto> // ???
}