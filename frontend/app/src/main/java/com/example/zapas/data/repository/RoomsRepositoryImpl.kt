package com.example.zapas.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.zapas.data.api.services.RoomsApiService
import com.example.zapas.data.api.util.NetworkErrorMapper
import com.example.zapas.data.repository.paging.RoomsPagingSource
import com.example.zapas.domain.models.Room
import com.example.zapas.domain.repository.RoomsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RoomsRepositoryImpl @Inject constructor(
    private val roomApi: RoomsApiService,
    private val networkErrorMapper: NetworkErrorMapper
) : RoomsRepository {
    override suspend fun getRoomsList(
        key: Int?, size: Int
    ) = networkErrorMapper.safeApiCallMapped { roomApi.rooms(key, size) }

    override fun getRoomsPaged(): Flow<PagingData<Room>> = Pager(
        config = PagingConfig(
            pageSize = 30, prefetchDistance = 5, initialLoadSize = 30
        ), pagingSourceFactory = {
            RoomsPagingSource(this)
        }).flow
}