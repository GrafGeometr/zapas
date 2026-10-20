package com.example.zapas.features.rooms.domain.usecase

import arrow.core.Either
import com.example.zapas.core.domain.model.AppError
import com.example.zapas.features.rooms.domain.model.Room
import com.example.zapas.features.rooms.domain.repository.RoomsRepository
import com.example.zapas.core.domain.UseCase
import javax.inject.Inject

class GetRoomProfileUseCase @Inject constructor(
    val roomsRepository: RoomsRepository
) : UseCase<String, Either<AppError, Room>>() {
    override suspend fun invoke(params: String) = roomsRepository.getRoomProfile(params)
}