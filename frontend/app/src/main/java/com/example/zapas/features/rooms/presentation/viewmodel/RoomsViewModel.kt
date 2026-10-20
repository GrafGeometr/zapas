package com.example.zapas.features.rooms.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.compose.LazyPagingItems
import com.example.zapas.core.domain.model.AppError
import com.example.zapas.features.rooms.domain.model.Room
import com.example.zapas.features.rooms.domain.usecase.GetRoomsPagedUseCase
import com.example.zapas.features.rooms.domain.usecase.JoinRoomUseCase
import com.example.zapas.core.utils.PagingSourceList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoomsUiState(
    val showJoinDialog: Boolean = false,
    val inviteTokenInput: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class RoomsViewModel @Inject constructor(
    getRoomsPagedUseCase: GetRoomsPagedUseCase,
    private val joinRoomUseCase: JoinRoomUseCase
) : ViewModel() {
    val rooms: Flow<PagingData<Room>> = getRoomsPagedUseCase().cachedIn(viewModelScope)

    private val _uiState = MutableStateFlow(RoomsUiState())
    val uiState = _uiState.asStateFlow()

    fun setShowJoinDialog(show: Boolean) {
        _uiState.update {
            it.copy(
                showJoinDialog = show,
                errorMessage = null,
                inviteTokenInput = "",
                isLoading = false
            )
        }
    }

    fun setInviteTokenInput(token: String) {
        _uiState.update { it.copy(inviteTokenInput = token, errorMessage = null) }
    }

    fun joinRoom(token: String, onSuccess: (String) -> Unit) {
        if (token.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = joinRoomUseCase(JoinRoomUseCase.Params(token.trim()))
            result.fold(
                ifLeft = { err ->
                    val message = when (err) {
                        is AppError.ApiError -> err.msg
                        is AppError.NetworkError -> "Ошибка сети. Проверьте соединение."
                        is AppError.UnknownError -> err.msg ?: "Неизвестная ошибка"
                    }
                    _uiState.update { it.copy(isLoading = false, errorMessage = message) }
                },
                ifRight = { roomId ->
                    _uiState.update { it.copy(isLoading = false, showJoinDialog = false) }
                    onSuccess(roomId)
                }
            )
        }
    }
}
