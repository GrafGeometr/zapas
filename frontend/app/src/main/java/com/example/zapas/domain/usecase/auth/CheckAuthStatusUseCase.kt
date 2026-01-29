package com.example.zapas.domain.usecase.auth

import com.example.zapas.domain.repository.AuthRepository
import com.example.zapas.domain.usecase.NoParamsUseCase
import javax.inject.Inject


class CheckAuthStatusUseCase @Inject constructor(
    private val authRepository: AuthRepository
) : NoParamsUseCase<Boolean>() {
    override suspend operator fun invoke(): Boolean {
        return authRepository.isLoggedIn()
    }
}