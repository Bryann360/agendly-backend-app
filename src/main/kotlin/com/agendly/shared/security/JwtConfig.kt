package com.agendly.shared.security

import com.nimbusds.jose.jwk.source.ImmutableSecret
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.*
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@Configuration
class JwtConfig {

  @Bean
  fun jwtEncoder(props: SecurityProps): JwtEncoder {
    val key: SecretKey = SecretKeySpec(props.jwtSecret.toByteArray(), "HmacSHA256")
    return NimbusJwtEncoder(ImmutableSecret(key))
  }

  @Bean
  fun jwtDecoder(props: SecurityProps): JwtDecoder {
    val key: SecretKey = SecretKeySpec(props.jwtSecret.toByteArray(), "HmacSHA256")
    return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build()
  }
}
