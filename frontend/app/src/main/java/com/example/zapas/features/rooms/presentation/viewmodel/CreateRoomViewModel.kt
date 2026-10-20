package com.example.zapas.features.rooms.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zapas.core.domain.model.AppError
import com.example.zapas.features.rooms.domain.usecase.CreateRoomUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CreateRoomState {
    object Idle : CreateRoomState()
    object Loading : CreateRoomState()
    data class Success(val id: String) : CreateRoomState()
    data class Error(val message: String) : CreateRoomState()
}

@HiltViewModel
class CreateRoomViewModel @Inject constructor(
    private val createRoomUseCase: CreateRoomUseCase
) : ViewModel() {
    private val _state = MutableStateFlow<CreateRoomState>(CreateRoomState.Idle)
    val state = _state.asStateFlow()

    fun createRoom(displayName: String, bio: String) {
        viewModelScope.launch {
            _state.value = CreateRoomState.Loading

            val result = createRoomUseCase(CreateRoomUseCase.Params(bio = bio, displayName = displayName))

            _state.value = result.fold(
                ifLeft = { err: AppError ->
                    val message = when (err) {
                        is AppError.ApiError -> err.msg
                        is AppError.NetworkError -> "Ошибка сети. Проверьте соединение."
                        is AppError.UnknownError -> err.msg ?: "Неизвестная ошибка"
                    }
                    CreateRoomState.Error(message)
                },
                ifRight = { CreateRoomState.Success(it) })
        }
    }
}
