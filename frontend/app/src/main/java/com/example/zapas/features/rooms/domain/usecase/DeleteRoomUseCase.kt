package com.example.zapas.features.rooms.domain.usecase

import arrow.core.Either
import com.example.zapas.core.domain.model.AppError
import com.example.zapas.features.rooms.domain.repository.RoomsRepository
import com.example.zapas.core.domain.UseCase
import javax.inject.Inject

class DeleteRoomUseCase @Inject constructor(
    private val roomsRepository: RoomsRepository
) : UseCase<String, Either<AppError, Unit>>() {
    override suspend fun invoke(params: String): Either<AppError, Unit> = roomsRepository.deleteRoom(params)
}
