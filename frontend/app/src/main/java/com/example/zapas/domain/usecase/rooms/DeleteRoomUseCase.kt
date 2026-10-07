package com.example.zapas.domain.usecase.rooms

import arrow.core.Either
import com.example.zapas.domain.models.AppError
import com.example.zapas.domain.repository.RoomsRepository
import com.example.zapas.domain.usecase.UseCase
import javax.inject.Inject

class DeleteRoomUseCase @Inject constructor(
    private val roomsRepository: RoomsRepository
) : UseCase<String, Either<AppError, Unit>>() {
    override suspend fun invoke(params: String): Either<AppError, Unit> {
        return roomsRepository.deleteRoom(params)
    }
}
