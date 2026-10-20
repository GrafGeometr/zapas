package com.example.zapas.features.rooms.data.repository.paging

import com.example.zapas.core.utils.DefaultPagingSource
import com.example.zapas.features.rooms.domain.model.Room
import com.example.zapas.features.rooms.domain.repository.RoomsRepository

class RoomsPagingSource(
    private val roomsRepository: RoomsRepository
) : DefaultPagingSource<Room>(getter = { it: LoadParams<Int> ->
    roomsRepository.getRoomsList(it.key, it.loadSize)
})
