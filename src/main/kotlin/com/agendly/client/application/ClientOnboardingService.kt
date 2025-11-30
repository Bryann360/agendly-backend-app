package com.agendly.client.application

import com.agendly.billing.BillingProps
import com.agendly.billing.infrastructure.persistence.entity.SubscriptionEntity
import com.agendly.billing.infrastructure.persistence.entity.SubscriptionPaymentEntity
import com.agendly.billing.infrastructure.persistence.repository.SubscriptionJpaRepository
import com.agendly.billing.infrastructure.persistence.repository.SubscriptionPaymentJpaRepository
import com.agendly.catalog.infrastructure.persistence.repository.PlanJpaRepository
import com.agendly.client.infrastructure.persistence.entity.ClientAccountEntity
import com.agendly.client.infrastructure.persistence.entity.TenantClientEntity
import com.agendly.client.infrastructure.persistence.repository.ClientAccountJpaRepository
import com.agendly.client.infrastructure.persistence.repository.TenantClientJpaRepository
import com.agendly.shared.errors.exceptions.BadRequestException
import com.agendly.shared.errors.exceptions.ConflictException
import com.agendly.shared.errors.exceptions.NotFoundException
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class ClientOnboardingService(
  private val billing: BillingProps,
  private val invites: InviteService,
  private val clientAccounts: ClientAccountJpaRepository,
  private val tenantClients: TenantClientJpaRepository,
  private val plans: PlanJpaRepository,
  private val subscriptions: SubscriptionJpaRepository,
  private val subscriptionPayments: SubscriptionPaymentJpaRepository
) {

  data class SignupViaInviteCommand(
    val tenantSlug: String,
    val inviteToken: String,
    val clientName: String,
    val clientEmail: String?,
    val clientPhone: String?,
    val planId: UUID
  )

  data class SignupResult(
    val tenantClientId: UUID,
    val subscriptionId: UUID,
    val subscriptionStatus: String
  )

  fun signupViaInvite(cmd: SignupViaInviteCommand): SignupResult {
    val invite = invites.validate(cmd.inviteToken)
    if (invite.tenant.slug != cmd.tenantSlug) throw BadRequestException("Invite does not belong to tenant")

    val plan = plans.findByIdAndTenantId(cmd.planId, invite.tenant.id!!)
      ?: throw NotFoundException("Plan not found")

    // must be allowed if invite preselected plan exists
    invite.preselectedPlan?.let {
      if (it.id != plan.id) throw BadRequestException("Invite requires a specific plan")
    }

    val acc = findOrCreateAccount(cmd.clientName, cmd.clientEmail, cmd.clientPhone)

    val existing = tenantClients.findByTenantIdAndClientAccountId(invite.tenant.id!!, acc.id!!)
    if (existing != null) throw ConflictException("Client already linked to this merchant")

    val link = TenantClientEntity().also {
      it.tenant = invite.tenant
      it.clientAccount = acc
      it.status = "ACTIVE"
      it.invitedBy = invite.createdBy
      it.joinedAt = Instant.now()
    }
    val tenantClient = tenantClients.save(link)

    // create subscription (beta flow: activate without charge if allowFreeSubscriptions OR price 0)
    val now = Instant.now()
    val periodEnd = now.plus(30, ChronoUnit.DAYS)
    val sub = SubscriptionEntity().also {
      it.tenant = invite.tenant
      it.tenantClientId = tenantClient.id!!
      it.plan = plan
      it.status = if (billing.allowFreeSubscriptions || plan.priceCents == 0L) "ACTIVE" else "PENDING"
      it.startedAt = now
      it.currentPeriodStart = now
      it.currentPeriodEnd = periodEnd
      it.nextBillingAt = periodEnd
      it.remainingCredits = plan.maxBookingsPerCycle
    }
    val savedSub = subscriptions.save(sub)

    if (savedSub.status == "ACTIVE") {
      val amount = plan.priceCents
      val platformFee = (amount * billing.platformFeeBps) / 10_000L
      val merchantFee = amount - platformFee

      subscriptionPayments.save(SubscriptionPaymentEntity().also { p ->
        p.id = UUID.randomUUID()
        p.tenantId = invite.tenant.id!!
        p.subscriptionId = savedSub.id!!
        p.provider = "INTERNAL"
        p.amountCents = amount
        p.platformFeeCents = platformFee
        p.merchantFeeCents = merchantFee
        p.status = "PAID"
        p.paidAt = Instant.now()
        p.createdAt = Instant.now()
      })
      invites.markUsed(invite)
    }

    return SignupResult(
      tenantClientId = tenantClient.id!!,
      subscriptionId = savedSub.id!!,
      subscriptionStatus = savedSub.status
    )
  }

  private fun findOrCreateAccount(name: String, email: String?, phone: String?): ClientAccountEntity {
    if (email != null) {
      val existed = clientAccounts.findByEmail(email.lowercase())
      if (existed != null) {
        // keep info fresh
        existed.name = name
        existed.phone = phone ?: existed.phone
        return clientAccounts.save(existed)
      }
    }

    val created = ClientAccountEntity().also {
      it.name = name
      it.email = email?.lowercase()
      it.phone = phone
    }
    return clientAccounts.save(created)
  }
}
