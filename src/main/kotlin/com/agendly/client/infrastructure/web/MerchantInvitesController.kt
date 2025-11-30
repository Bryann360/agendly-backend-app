package com.agendly.client.infrastructure.web

import com.agendly.client.application.InviteService
import jakarta.validation.constraints.Min
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.UUID

@RestController
@RequestMapping("/api/merchant/invites")
class MerchantInvitesController(
  private val invites: InviteService
) {

  data class CreateInviteRequest(
    val expiresAt: Instant? = null,
    @field:Min(1) val maxUses: Int = 1,
    val preselectedPlanId: UUID? = null
  )

  @PostMapping
  fun create(@RequestBody req: CreateInviteRequest): InviteService.InviteResult =
    invites.createInvite(
      InviteService.CreateInviteCommand(
        expiresAt = req.expiresAt,
        maxUses = req.maxUses,
        preselectedPlanId = req.preselectedPlanId
      )
    )
}
