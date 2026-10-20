package com.example.zapas.core.network.dto

interface ResponseDto<T> {
    fun toDomain(): T
}
