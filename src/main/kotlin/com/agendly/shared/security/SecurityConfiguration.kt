package com.agendly.shared.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfiguration {

  @Bean
  fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

  @Bean
  fun filterChain(http: HttpSecurity): SecurityFilterChain {
    http
      .cors { }
      .csrf { it.disable() }
      .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
      .authorizeHttpRequests {
        it.requestMatchers("/api/public/**").permitAll()
        it.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
        it.requestMatchers("/actuator/**").permitAll()
        it.requestMatchers("/api/merchant/auth/signup", "/api/merchant/auth/login").permitAll()
        it.requestMatchers("/api/public/**").permitAll()
        it.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
        it.anyRequest().authenticated()
      }
      .oauth2ResourceServer { it.jwt { j -> j.jwtAuthenticationConverter(jwtAuthConverter()) } }

    return http.build()
  }

  @Bean
  fun jwtAuthConverter(): JwtAuthenticationConverter {
    val converter = JwtAuthenticationConverter()
    converter.setJwtGrantedAuthoritiesConverter { jwt ->
      val role = jwt.getClaimAsString("role")
      val authorities = mutableListOf<GrantedAuthority>()
      if (!role.isNullOrBlank()) {
        authorities.add(SimpleGrantedAuthority(role))
      }
      authorities
    }
    return converter
  }
}
