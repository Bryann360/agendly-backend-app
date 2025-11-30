package com.agendly.shared.local

import com.agendly.catalog.infrastructure.persistence.entity.PlanEntity
import com.agendly.catalog.infrastructure.persistence.entity.PlanServiceEntity
import com.agendly.catalog.infrastructure.persistence.entity.PlanServiceId
import com.agendly.catalog.infrastructure.persistence.entity.ServiceEntity
import com.agendly.catalog.infrastructure.persistence.repository.PlanJpaRepository
import com.agendly.catalog.infrastructure.persistence.repository.PlanServiceJpaRepository
import com.agendly.catalog.infrastructure.persistence.repository.ServiceJpaRepository
import com.agendly.client.infrastructure.persistence.entity.ClientInviteEntity
import com.agendly.client.infrastructure.persistence.repository.ClientInviteJpaRepository
import com.agendly.identity.infrastructure.persistence.entity.MerchantUserEntity
import com.agendly.identity.infrastructure.persistence.repository.MerchantUserJpaRepository
import com.agendly.operations.infrastructure.persistence.entity.*
import com.agendly.operations.infrastructure.persistence.repository.BranchJpaRepository
import com.agendly.operations.infrastructure.persistence.repository.StaffMemberJpaRepository
import com.agendly.operations.infrastructure.persistence.repository.StaffServiceJpaRepository
import com.agendly.operations.infrastructure.persistence.repository.StaffWorkingHoursJpaRepository
import com.agendly.shared.crypto.Hashing
import com.agendly.shared.text.Slugify
import com.agendly.tenant.infrastructure.persistence.entity.TenantBrandingEntity
import com.agendly.tenant.infrastructure.persistence.entity.TenantEntity
import com.agendly.tenant.infrastructure.persistence.entity.TenantSettingsEntity
import com.agendly.tenant.infrastructure.persistence.repository.TenantBrandingJpaRepository
import com.agendly.tenant.infrastructure.persistence.repository.TenantJpaRepository
import com.agendly.tenant.infrastructure.persistence.repository.TenantSettingsJpaRepository
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.time.Instant
import java.time.LocalTime
import java.util.Base64
import java.util.UUID

/**
 * Dev seed runner:
 * - Creates a demo merchant (tenant + owner user)
 * - Adds branch, staff, services, plans and mappings
 * - Creates an invite token (printed in logs) so you can open the client flow immediately
 *
 * Idempotent: if tenant slug already exists, it does nothing.
 *
 * Run with:
 *   SPRING_PROFILES_ACTIVE=dev gradle bootRun
 */
@Profile("local")
@Component
class DevSeedRunner(
  // === Repos (adjust to your actual repo names) ===
  private val tenants: TenantJpaRepository,
  private val brandingRepo: TenantBrandingJpaRepository,
  private val settingsRepo: TenantSettingsJpaRepository,
  private val users: MerchantUserJpaRepository,

  private val branches: BranchJpaRepository,

  private val services: ServiceJpaRepository,
  private val plans: PlanJpaRepository,
  private val planServices: PlanServiceJpaRepository,

  private val staffRepo: StaffMemberJpaRepository,
  private val staffServices: StaffServiceJpaRepository,
  private val staffWorkingHours: StaffWorkingHoursJpaRepository,

  private val invites: ClientInviteJpaRepository,

  private val encoder: PasswordEncoder,

  @Value("\${agendly.dev.seed:true}") private val seedEnabled: Boolean
) : CommandLineRunner {

  private val log = LoggerFactory.getLogger(javaClass)

  @Transactional
  override fun run(vararg args: String?) {
    if (!seedEnabled) return

    val tenantName = "Demo Barbearia"
    val tenantSlug = Slugify.slugify(tenantName).ifBlank { "demo-barbearia" }

    if (tenants.findBySlug(tenantSlug) != null) {
      log.info("[DevSeed] Tenant '{}' already exists. Skipping seed.", tenantSlug)
      return
    }

    val tenant = tenants.save(TenantEntity().also {
      it.name = tenantName
      it.slug = tenantSlug
      it.status = "ACTIVE"
    })

    brandingRepo.save(TenantBrandingEntity().also { b ->
      b.tenant = tenant
      b.primaryColor = "#0F172A"
      b.secondaryColor = "#F59E0B"
      b.logoKey = "demo/logo.png"
      b.coverKey = "demo/cover.png"
    })

    settingsRepo.save(TenantSettingsEntity().also { s ->
      s.tenant = tenant
      s.timezone = "America/Sao_Paulo"
      s.locale = "pt-BR"
      s.currency = "BRL"
      s.requireActiveSubscriptionToBook = true
      s.bookingWindowDays = 60
      s.cancelMinNoticeHours = 6
      s.maxActiveBookingsPerClient = 3
    })

    val ownerEmail = "owner@demo.com"
    val ownerPassword = "ChangeMe123!"
    val owner = users.save(MerchantUserEntity().also { u ->
      u.tenant = tenant
      u.name = "Dono da Barbearia"
      u.email = ownerEmail
      u.passwordHash = encoder.encode(ownerPassword)
      u.role = "OWNER"
      u.status = "ACTIVE"
    })

    val branch = branches.save(BranchEntity().also { b ->
      b.tenant = tenant
      b.name = "Unidade Centro"
      b.timezone = "America/Sao_Paulo"
      b.addressStreet = "Av. Afonso Pena"
      b.addressNumber = "1000"
      b.addressCity = "Belo Horizonte"
      b.addressState = "MG"
      b.addressCountry = "BR"
      b.addressExtra = mutableMapOf<String, Any?>(
        "landmark" to "Praça Sete",
        "entrance" to "Back entrance",
        "parking" to mapOf(
          "available" to true,
          "notes" to "Use garage on Rua da Bahia"
        )
      )
      b.active = true
    })

    val corte = services.save(ServiceEntity().also { s ->
      s.tenant = tenant
      s.name = "Corte"
      s.description = "Corte masculino padrão"
      s.durationMin = 30
      s.active = true
    })

    val barba = services.save(ServiceEntity().also { s ->
      s.tenant = tenant
      s.name = "Barba"
      s.description = "Barba completa com toalha quente"
      s.durationMin = 30
      s.active = true
    })

    val premium = plans.save(PlanEntity().also { p ->
      p.tenant = tenant
      p.name = "Premium"
      p.description = "Corte + barba, 1x por semana"
      p.priceCents = 9900
      p.billingCycle = "MONTHLY"
      p.maxBookingsPerCycle = 4
      p.active = true
    })

    planServices.save(PlanServiceEntity(PlanServiceId(premium.id!!, corte.id!!)))
    planServices.save(PlanServiceEntity(PlanServiceId(premium.id!!, barba.id!!)))

    val joao = staffRepo.save(StaffMemberEntity().also { st ->
      st.tenant = tenant
      st.branchId = branch.id
      st.name = "João Barbeiro"
      st.active = true
    })

    staffServices.save(StaffServiceEntity(StaffServiceId(joao.id!!, corte.id!!)))
    staffServices.save(StaffServiceEntity(StaffServiceId(joao.id!!, barba.id!!)))

    listOf(1,2,3,4,5).forEach { dow ->
      staffWorkingHours.save(StaffWorkingHoursEntity().also { wh ->
        wh.id = UUID.randomUUID()
        wh.staffId = joao.id!!
        wh.branchId = branch.id
        wh.dayOfWeek = dow
        wh.startTime = LocalTime.of(9, 0)
        wh.endTime = LocalTime.of(18, 0)
        wh.createdAt = Instant.now()
      })
    }

    val rawToken = randomToken()
    val tokenHash = Hashing.sha256Hex(rawToken)

    invites.save(ClientInviteEntity().also { inv ->
      inv.id = UUID.randomUUID()
      inv.tenant = tenant
      inv.createdBy = owner
      inv.tokenHash = tokenHash
      inv.expiresAt = Instant.now().plusSeconds(60L * 60L * 24L * 30L)
      inv.maxUses = 50
      inv.usedCount = 0
      inv.preselectedPlan = null
      inv.createdAt = Instant.now()
    })

    log.info("[DevSeed] ✅ Demo tenant created")
    log.info("[DevSeed] Merchant login: tenantSlug={}, email={}, password={}", tenantSlug, ownerEmail, ownerPassword)
    log.info("[DevSeed] Invite token: {}", rawToken)
  }

  private fun randomToken(): String {
    val bytes = ByteArray(32)
    SecureRandom().nextBytes(bytes)
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
  }
}
