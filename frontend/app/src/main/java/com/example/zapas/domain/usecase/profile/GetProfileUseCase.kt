package com.example.zapas.domain.usecase.profile

import arrow.core.Either
import com.example.zapas.domain.models.AppError
import com.example.zapas.domain.models.User
import com.example.zapas.domain.repository.UserRepository
import com.example.zapas.domain.usecase.NoParamsUseCase
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) : NoParamsUseCase<Either<AppError, User>>() {
    override suspend fun invoke(): Either<AppError, User> {
        return userRepository.getMe()
    }
}
