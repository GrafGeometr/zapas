package com.example.zapas.domain.usecase.auth

import com.example.zapas.domain.repository.UserRepository
import com.example.zapas.domain.usecase.NoParamsUseCase
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val userRepository: UserRepository
) : NoParamsUseCase<Unit>() {
    override suspend fun invoke() {
        userRepository.logout()
    }
}
