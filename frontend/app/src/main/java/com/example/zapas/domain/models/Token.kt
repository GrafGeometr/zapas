package com.example.zapas.domain.models

sealed class Token {
    data class Access(val value: String) : Token()
    data class Refresh(val value: String) : Token()
    data class Admin(val value: String) : Token()
}


