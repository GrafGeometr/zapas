package com.example.zapas.domain.usecase.rooms

import arrow.core.Either
import com.example.zapas.domain.models.AppError
import com.example.zapas.domain.models.Room
import com.example.zapas.domain.repository.RoomsRepository
import com.example.zapas.domain.usecase.UseCase
import javax.inject.Inject

class GetRoomProfileUseCase @Inject constructor(
    val roomsRepository: RoomsRepository
) : UseCase<String, Either<AppError, Room>>() {
    override suspend fun invoke(params: String) = roomsRepository.getRoomProfile(params)
}