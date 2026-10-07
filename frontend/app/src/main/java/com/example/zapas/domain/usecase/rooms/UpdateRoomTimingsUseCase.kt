package com.example.zapas.domain.usecase.rooms

import arrow.core.Either
import com.example.zapas.domain.models.AppError
import com.example.zapas.domain.models.Room
import com.example.zapas.domain.repository.RoomsRepository
import com.example.zapas.domain.usecase.UseCase
import javax.inject.Inject

class UpdateRoomTimingsUseCase @Inject constructor(
    private val roomsRepository: RoomsRepository
) : UseCase<UpdateRoomTimingsUseCase.Params, Either<AppError, Room>>() {

    data class Params(
        val roomId: String,
        val expiredNotify: String?,
        val reviewInterval: String?
    )

    override suspend fun invoke(params: Params): Either<AppError, Room> {
        return roomsRepository.updateRoomTimings(
            roomId = params.roomId,
            expiredNotify = params.expiredNotify,
            reviewInterval = params.reviewInterval
        )
    }
}
