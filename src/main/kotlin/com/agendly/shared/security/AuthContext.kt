package com.agendly.shared.security

import com.agendly.shared.errors.exceptions.UnauthorizedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import java.util.UUID

object AuthContext {
  fun jwt(): Jwt = (SecurityContextHolder.getContext().authentication?.principal as? Jwt)
    ?: throw UnauthorizedException("Missing JWT")

  fun merchantUserId(): UUID = UUID.fromString(jwt().subject)

  fun tenantId(): UUID = UUID.fromString(jwt().getClaimAsString("tenant_id"))
}
