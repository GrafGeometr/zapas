package com.example.zapas.features.auth.data.dto.response

import com.example.zapas.core.network.dto.ResponseDto
import com.example.zapas.features.auth.domain.model.Token
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TokenDto(
    @Json(name = "token") val token: String
) : ResponseDto<Token> {
    override fun toDomain(): Token = Token.Basic(token)
}