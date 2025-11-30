package com.agendly.shared.security

import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID
import org.springframework.security.oauth2.jose.jws.MacAlgorithm

@Service
class JwtTokensService(
  private val encoder: JwtEncoder
) {
  private val ttlSeconds = 60L * 60L * 12L // 12h

  fun issueForMerchant(merchantUserId: UUID, tenantId: UUID, role: String): JwtTokens {
    val now = Instant.now()
    val claims = JwtClaimsSet.builder()
      .issuedAt(now)
      .expiresAt(now.plusSeconds(ttlSeconds))
      .subject(merchantUserId.toString())
      .claim("tenant_id", tenantId.toString())
      .claim("role", role)
      .claim("typ", "MERCHANT")
      .build()

    val headers = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build()
    val jwt = encoder.encode(JwtEncoderParameters.from(headers, claims))
    return JwtTokens(jwt.tokenValue, expiresInSeconds = ttlSeconds)
  }
}
