package com.example.zapas.domain.usecase.profile

import arrow.core.Either
import com.example.zapas.domain.models.AppError
import com.example.zapas.domain.models.User
import com.example.zapas.domain.repository.UserRepository
import com.example.zapas.domain.usecase.UseCase
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) : UseCase<UpdateProfileUseCase.Params, Either<AppError, User>>() {

    data class Params(
        val displayName: String?,
        val bio: String?,
        val username: String?
    )

    override suspend fun invoke(params: Params): Either<AppError, User> {
        return userRepository.updateProfile(
            displayName = params.displayName,
            bio = params.bio,
            username = params.username
        )
    }
}
