package com.example.zapas.features.auth.domain.usecase

import arrow.core.Either
import com.example.zapas.core.domain.model.AppError
import com.example.zapas.features.auth.domain.repository.AuthRepository
import com.example.zapas.core.domain.UseCase
import javax.inject.Inject

data class RegisterParams(
    val username: String,
    val password: String,
    val displayName: String,
    val bio: String
)

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) : UseCase<RegisterParams, Either<AppError, Unit>>() {
    override suspend fun invoke(params: RegisterParams): Either<AppError, Unit> = authRepository.register(
        params.username,
        params.password,
        params.displayName,
        params.bio
    )

}