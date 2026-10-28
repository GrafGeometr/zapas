package com.example.zapas.features.rooms.domain.usecase

import arrow.core.Either
import com.example.zapas.core.domain.model.AppError
import com.example.zapas.features.rooms.domain.repository.RoomsRepository
import com.example.zapas.core.domain.UseCase
import com.example.zapas.core.utils.PagingPage
import com.example.zapas.features.rooms.domain.model.Room
import javax.inject.Inject

class GetRoomsListUseCase @Inject constructor(
    private val roomsRepository: RoomsRepository
) : UseCase<GetRoomsListUseCase.Params, Either<AppError, PagingPage<Room>>>() {
    override suspend fun invoke(params: Params) =
        roomsRepository.getRoomsList(params.page, params.size)

    data class Params(
        val page: Int?, val size: Int
    )
}