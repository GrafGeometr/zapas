package com.example.zapas.features.auth.domain.usecase

import com.example.zapas.features.profile.domain.repository.UserRepository
import com.example.zapas.core.domain.NoParamsUseCase
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val userRepository: UserRepository
) : NoParamsUseCase<Unit>() {
    override suspend fun invoke() {
        userRepository.logout()
    }
}
