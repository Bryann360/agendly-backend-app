package com.agendly.shared.security

data class JwtTokens(
  val accessToken: String,
  val tokenType: String = "Bearer",
  val expiresInSeconds: Long
)
