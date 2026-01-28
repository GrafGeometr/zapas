package com.example.zapas.domain.models

sealed class Token {
    data class Verify(val value: String) : Token()
    data class Refresh(val value: String) : Token()
    data class Admin(val value: String) : Token()
    data class ForRoom(val value: String) : Token()
    data class Invite(val value: String) : Token()


}

data class TokenPair(
    val verify: Token.Verify,
    val refresh: Token.Refresh
)


