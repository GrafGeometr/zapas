package com.example.zapas.data.api.util

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.example.zapas.data.api.dto.response.AppErrJson
import com.example.zapas.domain.models.AppError
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Response

inline fun <reified T> Response<T>.toEither(): Either<AppError, T> {
    return if (isSuccessful) {
        val body = body()
        body?.right()
            ?: if (Unit is T) {
                Unit.right() as Either<AppError, T>
            } else {
                AppError.UnknownError("Empty response body").left()
            }
    } else {
        val errorBody = errorBody()?.string()
        val appError = try {
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val adapter = moshi.adapter(AppErrJson::class.java)
            val errorJson = adapter.fromJson(errorBody ?: "")
            if (errorJson != null) {
                AppError.ApiError(errorJson.code, errorJson.msg)
            } else {
                AppError.ApiError(code(), message())
            }
        } catch (e: Exception) {
            AppError.ApiError(code(), message())
        }
        appError.left()
    }
}

suspend inline fun <reified T> safeApiCall(crossinline call: suspend () -> Response<T>): Either<AppError, T> {
    return try {
        call().toEither()
    } catch (e: Exception) {
        AppError.NetworkError(e).left()
    }
}
