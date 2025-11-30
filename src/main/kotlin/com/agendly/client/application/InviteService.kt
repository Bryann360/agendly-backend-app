package com.agendly.client.application

import com.agendly.catalog.infrastructure.persistence.repository.PlanJpaRepository
import com.agendly.client.infrastructure.persistence.entity.ClientInviteEntity
import com.agendly.client.infrastructure.persistence.repository.ClientInviteJpaRepository
import com.agendly.identity.infrastructure.persistence.repository.MerchantUserJpaRepository
import com.agendly.shared.crypto.Hashing
import com.agendly.shared.errors.exceptions.BadRequestException
import com.agendly.shared.errors.exceptions.NotFoundException
import com.agendly.shared.security.AuthContext
import com.agendly.tenant.infrastructure.persistence.repository.TenantJpaRepository
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64
import java.util.UUID

@Service
class InviteService(
  private val tenants: TenantJpaRepository,
  private val users: MerchantUserJpaRepository,
  private val plans: PlanJpaRepository,
  private val invites: ClientInviteJpaRepository
) {

  data class CreateInviteCommand(
    val expiresAt: Instant? = null,
    val maxUses: Int = 1,
    val preselectedPlanId: UUID? = null
  )

  data class InviteResult(
    val inviteToken: String,
    val inviteUrl: String,
    val expiresAt: Instant?,
    val maxUses: Int
  )

  fun createInvite(cmd: CreateInviteCommand): InviteResult {
    val tenantId = AuthContext.tenantId()
    val userId = AuthContext.merchantUserId()
    val tenant = tenants.findById(tenantId).orElseThrow { NotFoundException("Tenant not found") }
    val user = users.findById(userId).orElseThrow { NotFoundException("User not found") }

    val preselectedPlan = cmd.preselectedPlanId?.let { pid ->
      plans.findByIdAndTenantId(pid, tenantId) ?: throw BadRequestException("Invalid planId")
    }

    val rawToken = randomToken()
    val tokenHash = Hashing.sha256Hex(rawToken)

    val invite = ClientInviteEntity().also {
      it.id = UUID.randomUUID()
      it.tenant = tenant
      it.createdBy = user
      it.tokenHash = tokenHash
      it.expiresAt = cmd.expiresAt
      it.maxUses = cmd.maxUses
      it.usedCount = 0
      it.preselectedPlan = preselectedPlan
      it.createdAt = Instant.now()
    }

    invites.save(invite)

    val url = "https://app.agendly.local/$${tenant.slug}/invite?token=$rawToken"
    return InviteResult(inviteToken = rawToken, inviteUrl = url, expiresAt = cmd.expiresAt, maxUses = cmd.maxUses)
  }

  fun validate(token: String): ClientInviteEntity {
    val h = Hashing.sha256Hex(token)
    val i = invites.findByTokenHash(h) ?: throw NotFoundException("Invite not found")
    if (i.expiresAt != null && Instant.now().isAfter(i.expiresAt)) throw BadRequestException("Invite expired")
    if (i.usedCount >= i.maxUses) throw BadRequestException("Invite already used")
    return i
  }

  fun markUsed(invite: ClientInviteEntity) {
    invite.usedCount += 1
    invites.save(invite)
  }

  private fun randomToken(): String {
    val bytes = ByteArray(32)
    SecureRandom().nextBytes(bytes)
    // URL-safe token
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
  }
}
