package com.example.zapas.features.rooms.domain.usecase

import arrow.core.Either
import com.example.zapas.core.domain.model.AppError
import com.example.zapas.features.rooms.domain.repository.RoomsRepository
import com.example.zapas.core.domain.UseCase
import javax.inject.Inject

class GetInviteTokenUseCase @Inject constructor(
    private val roomsRepository: RoomsRepository
) : UseCase<String, Either<AppError, String>>() {
    override suspend fun invoke(params: String): Either<AppError, String> = roomsRepository.getInviteToken(params)
}
