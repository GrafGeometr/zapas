package com.example.zapas.features.profile.domain.usecase

import arrow.core.Either
import com.example.zapas.core.domain.model.AppError
import com.example.zapas.features.profile.domain.repository.UserRepository
import com.example.zapas.core.domain.UseCase
import javax.inject.Inject

class DeleteUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) : UseCase<String, Either<AppError, Unit>>() {
    override suspend fun invoke(params: String): Either<AppError, Unit> = userRepository.deleteUser(params)
}
