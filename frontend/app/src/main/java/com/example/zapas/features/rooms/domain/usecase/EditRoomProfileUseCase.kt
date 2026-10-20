package com.example.zapas.features.rooms.domain.usecase

import arrow.core.Either
import com.example.zapas.core.domain.model.AppError
import com.example.zapas.features.rooms.domain.model.Room
import com.example.zapas.features.rooms.domain.repository.RoomsRepository
import com.example.zapas.core.domain.UseCase
import javax.inject.Inject

class EditRoomProfileUseCase @Inject constructor(
    private val roomsRepository: RoomsRepository
) : UseCase<EditRoomProfileUseCase.Params, Either<AppError, Room>>() {

    data class Params(
        val roomId: String,
        val displayName: String?,
        val bio: String?
    )

    override suspend fun invoke(params: Params): Either<AppError, Room> = roomsRepository.editRoomProfile(
        roomId = params.roomId,
        displayName = params.displayName,
        bio = params.bio
    )
}
