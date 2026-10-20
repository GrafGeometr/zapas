package com.example.zapas.features.rooms.domain.usecase

import arrow.core.Either
import com.example.zapas.core.domain.model.AppError
import com.example.zapas.features.rooms.domain.repository.RoomsRepository
import com.example.zapas.core.domain.UseCase
import javax.inject.Inject

class JoinRoomUseCase @Inject constructor(
    val roomsRepository: RoomsRepository
) : UseCase<JoinRoomUseCase.Params, Either<AppError, String>>() {
    override suspend fun invoke(params: Params) =
        roomsRepository.joinRoom(params.token)

    data class Params(
        val token: String
    )
}
