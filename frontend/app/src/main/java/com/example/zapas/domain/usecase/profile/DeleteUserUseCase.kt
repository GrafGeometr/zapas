package com.example.zapas.domain.usecase.profile

import arrow.core.Either
import com.example.zapas.domain.models.AppError
import com.example.zapas.domain.repository.UserRepository
import com.example.zapas.domain.usecase.UseCase
import javax.inject.Inject

class DeleteUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) : UseCase<String, Either<AppError, Unit>>() {
    override suspend fun invoke(params: String): Either<AppError, Unit> {
        return userRepository.deleteUser(params)
    }
}
