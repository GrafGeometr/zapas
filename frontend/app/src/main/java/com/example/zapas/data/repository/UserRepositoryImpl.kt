package com.example.zapas.data.repository

import com.example.zapas.data.api.dto.request.ChangePasswordRequestDto
import com.example.zapas.data.api.dto.request.UpdateProfileRequestDto
import com.example.zapas.data.api.dto.response.UuidDto
import com.example.zapas.data.api.services.UserApiService
import com.example.zapas.data.api.util.NetworkErrorMapper
import com.example.zapas.data.local.TokenManager
import com.example.zapas.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApiService,
    private val tokenManager: TokenManager,
    private val networkErrorMapper: NetworkErrorMapper
) : UserRepository {

    override suspend fun getMe() = networkErrorMapper.safeApiCallMapped { userApi.me() }

    override suspend fun updateProfile(
        displayName: String?, bio: String?, username: String?
    ) = networkErrorMapper.safeApiCallMapped {
        userApi.updateProfile(
            UpdateProfileRequestDto(
                displayName = displayName, bio = bio, username = username
            )
        )
    }

    override suspend fun changePassword(oldPasswd: String, newPasswd: String) =
        networkErrorMapper.safeApiCall {
            userApi.updatePassword(ChangePasswordRequestDto(oldPasswd, newPasswd))
        }

    override suspend fun deleteUser(userId: String) = networkErrorMapper.safeApiCall {
        userApi.deleteUser(UuidDto(userId))
    }

    override suspend fun logout() {
        try {
            userApi.logout()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        tokenManager.clearTokens()
    }
}
