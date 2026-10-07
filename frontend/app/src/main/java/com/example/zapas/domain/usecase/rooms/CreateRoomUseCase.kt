package com.example.zapas.domain.usecase.rooms

import arrow.core.Either
import com.example.zapas.domain.models.AppError
import com.example.zapas.domain.repository.RoomsRepository
import com.example.zapas.domain.usecase.UseCase
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