package com.agendly.shared.security

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "agendly.security")
data class SecurityProps(
  val jwtSecret: String
)
