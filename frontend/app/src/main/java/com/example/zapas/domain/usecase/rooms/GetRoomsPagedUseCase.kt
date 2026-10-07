package com.example.zapas.domain.usecase.rooms

import androidx.paging.PagingData
import com.example.zapas.domain.models.Room
import com.example.zapas.domain.repository.RoomsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRoomsPagedUseCase @Inject constructor(
    private val roomsRepository: RoomsRepository
) {
    operator fun invoke(): Flow<PagingData<Room>> = roomsRepository.getRoomsPaged()
}