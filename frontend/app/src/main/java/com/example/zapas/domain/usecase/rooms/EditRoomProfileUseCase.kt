package com.example.zapas.domain.usecase.rooms

import arrow.core.Either
import com.example.zapas.domain.models.AppError
import com.example.zapas.domain.models.Room
import com.example.zapas.domain.repository.RoomsRepository
import com.example.zapas.domain.usecase.UseCase
import javax.inject.Inject

class EditRoomProfileUseCase @Inject constructor(
    private val roomsRepository: RoomsRepository
) : UseCase<EditRoomProfileUseCase.Params, Either<AppError, Room>>() {

    data class Params(
        val roomId: String,
        val displayName: String?,
        val bio: String?
    )

    override suspend fun invoke(params: Params): Either<AppError, Room> {
        return roomsRepository.editRoomProfile(
            roomId = params.roomId,
            displayName = params.displayName,
            bio = params.bio
        )
    }
}
