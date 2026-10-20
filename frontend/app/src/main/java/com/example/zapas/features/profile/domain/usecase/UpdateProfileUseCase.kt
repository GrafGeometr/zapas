package com.example.zapas.features.profile.domain.usecase

import arrow.core.Either
import com.example.zapas.core.domain.model.AppError
import com.example.zapas.features.profile.domain.model.User
import com.example.zapas.features.profile.domain.repository.UserRepository
import com.example.zapas.core.domain.UseCase
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) : UseCase<UpdateProfileUseCase.Params, Either<AppError, User>>() {

    data class Params(
        val displayName: String?,
        val bio: String?,
        val username: String?
    )

    override suspend fun invoke(params: Params): Either<AppError, User> = userRepository.updateProfile(
        displayName = params.displayName,
        bio = params.bio,
        username = params.username
    )
}
