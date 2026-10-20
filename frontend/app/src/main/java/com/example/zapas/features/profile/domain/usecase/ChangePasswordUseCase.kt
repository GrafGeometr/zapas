package com.example.zapas.features.profile.domain.usecase

import arrow.core.Either
import com.example.zapas.core.domain.model.AppError
import com.example.zapas.features.profile.domain.repository.UserRepository
import com.example.zapas.core.domain.UseCase
import javax.inject.Inject

class ChangePasswordUseCase @Inject constructor(
    private val userRepository: UserRepository
) : UseCase<ChangePasswordUseCase.Params, Either<AppError, Unit>>() {

    data class Params(
        val oldPasswd: String,
        val newPasswd: String
    )

    override suspend fun invoke(params: Params): Either<AppError, Unit> =
        userRepository.changePassword(params.oldPasswd, params.newPasswd)
}
