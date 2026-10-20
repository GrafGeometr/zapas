package com.example.zapas.features.rooms.domain.usecase

import androidx.paging.PagingData
import com.example.zapas.features.rooms.domain.model.Member
import com.example.zapas.features.rooms.domain.repository.RoomsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMembersPagedUseCase @Inject constructor(
    private val roomsRepository: RoomsRepository
) {
    operator fun invoke(params: Params): Flow<PagingData<Member>> =
        roomsRepository.getMembersPaged(params.roomId)

    data class Params(
        val roomId: String
    )
}