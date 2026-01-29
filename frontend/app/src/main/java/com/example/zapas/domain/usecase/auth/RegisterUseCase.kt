package com.example.zapas.domain.usecase.auth

import com.example.zapas.domain.repository.AuthRepository
import com.example.zapas.domain.usecase.UseCase
import javax.inject.Inject

data class RegisterParams(
    val username: String,
    val password: String,
    val displayName: String
)

sealed class RegisterResult {
    object Success : RegisterResult()
    data class Error(val message: String) : RegisterResult()
}

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) : UseCase<RegisterParams, RegisterResult>() {
    override suspend fun invoke(params: RegisterParams): RegisterResult {
        return try {
            val success = authRepository.register(params.username, params.password, params.displayName)
            if (success) {
                RegisterResult.Success
            } else {
                RegisterResult.Error("Ошибка на этапе регистрации")
            }
        } catch (e: Exception) {
            RegisterResult.Error("Ошибка сети: ${e.message}")
        }
    }

}