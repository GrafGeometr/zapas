package com.example.zapas.domain.usecase.auth


import com.example.zapas.domain.repository.AuthRepository
import com.example.zapas.domain.usecase.UseCase
import javax.inject.Inject


data class LoginParams(
    val username: String, val password: String
)


sealed class LoginResult {
    object Success : LoginResult()
    data class Error(val message: String) : LoginResult()
}


class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) : UseCase<LoginParams, LoginResult>() {
    override suspend operator fun invoke(params: LoginParams): LoginResult {
        return try {
            val success = authRepository.login(params.username, params.password)
            if (success) {
                LoginResult.Success
            } else {
                LoginResult.Error("Неверный логин или пароль")
            }
        } catch (e: Exception) {
            LoginResult.Error("Ошибка сети: ${e.message}")
        }
    }
}