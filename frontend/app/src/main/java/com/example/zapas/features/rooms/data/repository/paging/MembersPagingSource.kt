package com.example.zapas.features.rooms.data.repository.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import arrow.core.Either
import com.example.zapas.core.domain.model.AppError
import com.example.zapas.core.utils.DefaultPagingSource
import com.example.zapas.core.utils.PagingPage
import com.example.zapas.features.rooms.domain.model.Member
import com.example.zapas.features.rooms.domain.model.MembersPage
import com.example.zapas.features.rooms.domain.repository.RoomsRepository

class MembersPagingSource(
    private val roomsRepository: RoomsRepository,
    private val roomId: String
) : DefaultPagingSource<Member>(
    getter = { it: LoadParams<Int> ->
        roomsRepository.getRoomMembers(roomId, it.key, it.loadSize)
    }
)