package com.example.zapas.domain.usecase.auth


import arrow.core.Either
import com.example.zapas.domain.models.AppError
import com.example.zapas.domain.repository.AuthRepository
import com.example.zapas.domain.usecase.UseCase
import javax.inject.Inject


data class LoginParams(
    val username: String, val password: String
)


class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) : UseCase<LoginParams, Either<AppError, Unit>>() {
    override suspend operator fun invoke(params: LoginParams): Either<AppError, Unit> {
        return authRepository.login(params.username, params.password)
    }
}