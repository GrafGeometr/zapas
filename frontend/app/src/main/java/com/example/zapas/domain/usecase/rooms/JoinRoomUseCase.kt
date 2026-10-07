package com.example.zapas.domain.usecase.rooms

import arrow.core.Either
import com.example.zapas.domain.models.AppError
import com.example.zapas.domain.repository.RoomsRepository
import com.example.zapas.domain.usecase.UseCase
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
