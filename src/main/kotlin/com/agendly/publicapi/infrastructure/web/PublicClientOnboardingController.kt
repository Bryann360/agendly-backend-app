package com.agendly.publicapi.infrastructure.web

import com.agendly.client.application.ClientOnboardingService
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/public/{tenantSlug}/client")
@Validated
class PublicClientOnboardingController(
  private val onboarding: ClientOnboardingService
) {

  data class SignupRequest(
    @field:NotBlank val inviteToken: String,
    @field:NotBlank val name: String,
    @field:Email val email: String? = null,
    val phone: String? = null,
    val planId: UUID
  )

  @PostMapping("/signup")
  fun signup(@PathVariable tenantSlug: String, @RequestBody req: SignupRequest): ClientOnboardingService.SignupResult =
    onboarding.signupViaInvite(
      ClientOnboardingService.SignupViaInviteCommand(
        tenantSlug = tenantSlug,
        inviteToken = req.inviteToken,
        clientName = req.name,
        clientEmail = req.email,
        clientPhone = req.phone,
        planId = req.planId
      )
    )
}
