package com.example.zapas.domain.usecase.profile

import arrow.core.Either
import com.example.zapas.domain.models.AppError
import com.example.zapas.domain.repository.UserRepository
import com.example.zapas.domain.usecase.UseCase
import javax.inject.Inject

class ChangePasswordUseCase @Inject constructor(
    private val userRepository: UserRepository
) : UseCase<ChangePasswordUseCase.Params, Either<AppError, Unit>>() {

    data class Params(
        val oldPasswd: String,
        val newPasswd: String
    )

    override suspend fun invoke(params: Params): Either<AppError, Unit> {
        return userRepository.changePassword(params.oldPasswd, params.newPasswd)
    }
}
