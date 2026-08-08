package com.example.zapas.domain.usecase.auth

import arrow.core.Either
import com.example.zapas.domain.models.AppError
import com.example.zapas.domain.repository.AuthRepository
import com.example.zapas.domain.usecase.UseCase
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
    override suspend fun invoke(params: RegisterParams): Either<AppError, Unit> {
        return authRepository.register(
            params.username,
            params.password,
            params.displayName,
            params.bio
        )
    }

}