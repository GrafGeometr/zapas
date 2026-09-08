package com.example.zapas.data.repository

import arrow.core.Either
import com.example.zapas.data.api.dto.request.EmailDto
import com.example.zapas.data.api.services.EmailApiService
import com.example.zapas.data.api.util.NetworkErrorMapper
import com.example.zapas.domain.models.AppError
import com.example.zapas.domain.repository.EmailRepository
import javax.inject.Inject

class EmailRepositoryImpl @Inject constructor(
    private val emailApi: EmailApiService,
    private val networkErrorMapper: NetworkErrorMapper
) : EmailRepository {
    override suspend fun resetPassword(email: String): Either<AppError, Unit> = networkErrorMapper.safeApiCall {
        emailApi.forgotPassword(EmailDto(email))
    }

    override suspend fun updateEmail(email: String): Either<AppError, Unit> = networkErrorMapper.safeApiCall {
        emailApi.updateEmail(EmailDto(email))
    }

    override suspend fun verifyEmail(): Either<AppError, Unit> = networkErrorMapper.safeApiCall {
        emailApi.verifyEmail()
    }
}
