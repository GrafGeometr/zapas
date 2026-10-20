package com.example.zapas.features.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zapas.core.domain.model.AppError
import com.example.zapas.features.profile.domain.model.User
import com.example.zapas.features.auth.domain.usecase.LogoutUseCase
import com.example.zapas.features.profile.domain.usecase.ChangePasswordUseCase
import com.example.zapas.features.profile.domain.usecase.DeleteUserUseCase
import com.example.zapas.features.profile.domain.usecase.GetProfileUseCase
import com.example.zapas.features.profile.domain.usecase.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEditing: Boolean = false,
    val showPasswordDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val updateSuccess: Boolean = false,
    val passwordChangeSuccess: Boolean = false,
    val deleteSuccess: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getProfileUseCase().fold(
                { error ->
                    _state.update { it.copy(isLoading = false, error = formatError(error)) }
                },
                { user ->
                    _state.update { it.copy(isLoading = false, user = user) }
                }
            )
        }
    }

    fun toggleEdit() {
        _state.update { it.copy(isEditing = !it.isEditing, updateSuccess = false) }
    }

    fun setShowPasswordDialog(show: Boolean) {
        _state.update { it.copy(showPasswordDialog = show) }
    }

    fun setShowDeleteDialog(show: Boolean) {
        _state.update { it.copy(showDeleteDialog = show) }
    }

    fun updateProfile(displayName: String, bio: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            updateProfileUseCase(
                UpdateProfileUseCase.Params(
                    displayName = displayName,
                    bio = bio,
                    username = null // API doesn't allow changing username in this call based on DTO, but spec had it.
                )
            ).fold(
                { error ->
                    _state.update { it.copy(isLoading = false, error = formatError(error)) }
                },
                { user ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            user = user,
                            isEditing = false,
                            updateSuccess = true
                        )
                    }
                }
            )
        }
    }

    fun changePassword(oldPasswd: String, newPasswd: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, passwordChangeSuccess = false) }
            changePasswordUseCase(
                ChangePasswordUseCase.Params(oldPasswd, newPasswd)
            ).fold(
                { error ->
                    _state.update { it.copy(isLoading = false, error = formatError(error)) }
                },
                {
                    _state.update { it.copy(isLoading = false, passwordChangeSuccess = true) }
                }
            )
        }
    }

    fun deleteAccount(onDeleted: () -> Unit) {
        val userId = _state.value.user?.id ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            deleteUserUseCase(userId).fold(
                { error ->
                    _state.update { it.copy(isLoading = false, error = formatError(error)) }
                },
                {
                    _state.update { it.copy(isLoading = false, deleteSuccess = true) }
                    logout(onDeleted)
                }
            )
        }
    }

    fun logout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            logoutUseCase()
            onLogoutComplete()
        }
    }

    private fun formatError(error: AppError): String? {
        return when (error) {
            is AppError.ApiError -> error.message
            is AppError.NetworkError -> "Ошибка сети. Проверьте подключение."
            is AppError.UnknownError -> error.message ?: "Произошла неизвестная ошибка"
        }
    }
}
