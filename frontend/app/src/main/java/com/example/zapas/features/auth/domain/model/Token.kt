package com.example.zapas.features.auth.domain.model

sealed class Token {
    data class Basic(val value: String) : Token()
}