package com.example.zapas.features.rooms.domain.usecase

import arrow.core.Either
import com.example.zapas.core.domain.UseCase
import com.example.zapas.core.domain.model.AppError
import com.example.zapas.core.utils.PagingPage
import com.example.zapas.features.rooms.domain.model.Member
import com.example.zapas.features.rooms.domain.repository.RoomsRepository
import javax.inject.Inject

class GetMembersUseCase @Inject constructor(
    private val roomsRepository: RoomsRepository
) : UseCase<GetMembersUseCase.Params, Either<AppError, PagingPage<Member>>>() {
    override suspend fun invoke(params: Params): Either<AppError, PagingPage<Member>> =
        roomsRepository.getRoomMembers(params.roomId, params.page, params.size)

    data class Params(
        val roomId: String, val page: Int?, val size: Int
    )
}