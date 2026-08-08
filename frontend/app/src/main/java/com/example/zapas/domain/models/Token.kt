package com.example.zapas.domain.models

sealed class Token {
    data class Basic(val value: String) : Token()
}


