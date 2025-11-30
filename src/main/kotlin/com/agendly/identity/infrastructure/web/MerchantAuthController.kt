package com.agendly.identity.infrastructure.web

import com.agendly.identity.application.MerchantAuthService
import com.agendly.shared.security.JwtTokens
import com.agendly.shared.text.Slugify
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/merchant/auth")
@Validated
class MerchantAuthController(
  private val auth: MerchantAuthService
) {

  data class SignupRequest(
    @field:NotBlank val tenantName: String,
    val tenantSlug: String? = null,
    @field:NotBlank val ownerName: String,
    @field:NotBlank @field:Email val ownerEmail: String,
    @field:NotBlank @field:Size(min = 8) val password: String
  )

  @PostMapping("/signup")
  fun signup(@RequestBody req: SignupRequest): JwtTokens {
    val slug = req.tenantSlug?.takeIf { it.isNotBlank() } ?: Slugify.slugify(req.tenantName)
    return auth.signup(
      MerchantAuthService.SignupCommand(
        tenantName = req.tenantName,
        tenantSlug = slug,
        ownerName = req.ownerName,
        ownerEmail = req.ownerEmail,
        password = req.password
      )
    )
  }

  data class LoginRequest(
    @field:NotBlank @field:Email val email: String,
    @field:NotBlank val password: String
  )

  @PostMapping("/login")
  fun login(@RequestBody req: LoginRequest): JwtTokens =
    auth.login(MerchantAuthService.LoginCommand(req.email, req.password))
}
