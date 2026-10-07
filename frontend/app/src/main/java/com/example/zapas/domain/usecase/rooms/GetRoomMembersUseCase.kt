package com.example.zapas.domain.usecase.rooms

import arrow.core.Either
import com.example.zapas.data.api.dto.response.MemberDto
import com.example.zapas.domain.models.AppError
import com.example.zapas.domain.repository.RoomsRepository
import com.example.zapas.domain.usecase.UseCase
import javax.inject.Inject

class GetRoomMembersUseCase @Inject constructor(
    private val roomsRepository: RoomsRepository
) : UseCase<GetRoomMembersUseCase.Params, Either<AppError, List<MemberDto>>>() {

    data class Params(
        val roomId: String,
        val num: Int = 0,
        val size: Int = 50
    )

    override suspend fun invoke(params: Params): Either<AppError, List<MemberDto>> {
        return roomsRepository.getRoomMembers(params.roomId, params.num, params.size)
    }
}
