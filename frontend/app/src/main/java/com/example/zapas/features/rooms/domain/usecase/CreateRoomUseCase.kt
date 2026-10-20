package com.example.zapas.features.rooms.domain.usecase

import arrow.core.Either
import com.example.zapas.core.domain.model.AppError
import com.example.zapas.features.rooms.domain.repository.RoomsRepository
import com.example.zapas.core.domain.UseCase
import javax.inject.Inject

class CreateRoomUseCase @Inject constructor(
    val roomsRepository: RoomsRepository
) : UseCase<CreateRoomUseCase.Params, Either<AppError, String>>() {
    override suspend fun invoke(params: Params) =
        roomsRepository.createRoom(params.bio, params.displayName)

    data class Params(
        val bio: String, val displayName: String
    )
}