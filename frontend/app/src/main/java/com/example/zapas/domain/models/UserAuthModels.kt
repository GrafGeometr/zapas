package com.example.zapas.domain.models


data class User(
    val uuid: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val description: String = ""
)

data class UserLogin(
    val username: String,
    val password: String
)

data class UserRegister(
    val userLogin: UserLogin,
    val userProfile: UserProfile
)

data class UserProfile(
    val firstName: String,
    val lastName: String,
    val description: String = ""
)
