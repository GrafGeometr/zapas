package com.example.zapas.domain.usecase.rooms

import arrow.core.Either
import com.example.zapas.domain.models.AppError
import com.example.zapas.domain.models.RoomsPage
import com.example.zapas.domain.repository.RoomsRepository
import com.example.zapas.domain.usecase.UseCase
import javax.inject.Inject

class GetRoomsListUseCase @Inject constructor(
    val roomsRepository: RoomsRepository
) : UseCase<GetRoomsListUseCase.Params, Either<AppError, RoomsPage>>() {
    override suspend fun invoke(params: Params) =
        roomsRepository.getRoomsList(params.key, params.size)

    data class Params(
        val key: Int?, val size: Int
    )
}