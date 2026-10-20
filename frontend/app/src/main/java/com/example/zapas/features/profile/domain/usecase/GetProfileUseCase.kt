package com.example.zapas.features.profile.domain.usecase

import arrow.core.Either
import com.example.zapas.core.domain.model.AppError
import com.example.zapas.features.profile.domain.model.User
import com.example.zapas.features.profile.domain.repository.UserRepository
import com.example.zapas.core.domain.NoParamsUseCase
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) : NoParamsUseCase<Either<AppError, User>>() {
    override suspend fun invoke(): Either<AppError, User> = userRepository.getMe()
}
