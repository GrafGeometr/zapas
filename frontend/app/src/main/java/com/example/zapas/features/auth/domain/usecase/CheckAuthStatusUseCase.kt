package com.example.zapas.features.auth.domain.usecase

import com.example.zapas.features.auth.domain.repository.AuthRepository
import com.example.zapas.core.domain.NoParamsUseCase
import javax.inject.Inject


class CheckAuthStatusUseCase @Inject constructor(
    private val authRepository: AuthRepository
) : NoParamsUseCase<Boolean>() {
    override suspend operator fun invoke(): Boolean = authRepository.isLoggedIn()
}