package com.example.zapas.data.api.dto.response

import com.example.zapas.domain.models.Token
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TokenDto(
    @Json(name = "token") val token: String
) : ResponseDto<Token> {
    override fun toDomain(): Token = Token.Basic(token)
}
