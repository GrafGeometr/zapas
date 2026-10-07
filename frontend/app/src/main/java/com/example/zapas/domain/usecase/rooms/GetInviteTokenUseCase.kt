package com.example.zapas.domain.usecase.rooms

import arrow.core.Either
import com.example.zapas.domain.models.AppError
import com.example.zapas.domain.repository.RoomsRepository
import com.example.zapas.domain.usecase.UseCase
import javax.inject.Inject

class GetInviteTokenUseCase @Inject constructor(
    private val roomsRepository: RoomsRepository
) : UseCase<String, Either<AppError, String>>() {
    override suspend fun invoke(params: String): Either<AppError, String> {
        return roomsRepository.getInviteToken(params)
    }
}
