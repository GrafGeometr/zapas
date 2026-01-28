package com.example.zapas.data.api.dto.request

data class RegisterRequestDto(
    val login: UserLoginDto,
    val display_name: String  // Так называется в API
)

data class UserLoginDto(
    val username: String,
    val passwd: String
)
