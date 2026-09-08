package com.example.zapas.domain.models

sealed class AppError(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause) {
    data class ApiError(val code: Int, val msg: String) : AppError("API Error $code: $msg")
    data class NetworkError(val throwable: Throwable) : AppError("Network Error", throwable)
    data class UnknownError(val msg: String? = null) : AppError(msg ?: "Unknown Error")
}
