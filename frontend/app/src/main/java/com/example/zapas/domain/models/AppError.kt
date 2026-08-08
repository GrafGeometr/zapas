package com.example.zapas.domain.models

sealed class AppError {
    data class ApiError(val code: Int, val message: String) : AppError()
    data class NetworkError(val throwable: Throwable) : AppError()
    data class UnknownError(val message: String? = null) : AppError()
}
