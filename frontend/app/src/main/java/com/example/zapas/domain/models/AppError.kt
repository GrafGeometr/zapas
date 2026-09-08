package com.example.zapas.domain.models

sealed class AppError(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {
    data class ApiError(val code: Int, val msg: String) : AppError(msg)
    data class NetworkError(val throwable: Throwable) : AppError(throwable.message, throwable)
    data class UnknownError(val msg: String? = null) : AppError(msg)
}
