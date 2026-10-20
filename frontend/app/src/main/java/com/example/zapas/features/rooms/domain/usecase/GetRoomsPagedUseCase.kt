package com.example.zapas.features.rooms.domain.usecase

import androidx.paging.PagingData
import com.example.zapas.features.rooms.domain.model.Room
import com.example.zapas.features.rooms.domain.repository.RoomsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRoomsPagedUseCase @Inject constructor(
    private val roomsRepository: RoomsRepository
) {
    operator fun invoke(): Flow<PagingData<Room>> = roomsRepository.getRoomsPaged()
}