package com.example.zapas.data.api.dto.response

interface ResponseDto<T> {
    fun toDomain(): T
}