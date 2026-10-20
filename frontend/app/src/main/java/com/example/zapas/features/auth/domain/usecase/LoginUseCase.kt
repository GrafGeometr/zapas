package com.example.zapas.features.auth.domain.usecase


import arrow.core.Either
import com.example.zapas.core.domain.model.AppError
import com.example.zapas.features.auth.domain.repository.AuthRepository
import com.example.zapas.core.domain.UseCase
import javax.inject.Inject


data class LoginParams(
    val username: String, val password: String
)


class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) : UseCase<LoginParams, Either<AppError, Unit>>() {
    override suspend operator fun invoke(params: LoginParams): Either<AppError, Unit> =
        authRepository.login(params.username, params.password)
}