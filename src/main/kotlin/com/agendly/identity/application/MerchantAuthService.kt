package com.agendly.identity.application

import com.agendly.identity.infrastructure.persistence.entity.MerchantUserEntity
import com.agendly.identity.infrastructure.persistence.repository.MerchantUserJpaRepository
import com.agendly.shared.errors.exceptions.ConflictException
import com.agendly.shared.errors.exceptions.UnauthorizedException
import com.agendly.shared.security.JwtTokens
import com.agendly.shared.security.JwtTokensService
import com.agendly.tenant.infrastructure.persistence.entity.TenantBrandingEntity
import com.agendly.tenant.infrastructure.persistence.entity.TenantEntity
import com.agendly.tenant.infrastructure.persistence.entity.TenantSettingsEntity
import com.agendly.tenant.infrastructure.persistence.repository.TenantBrandingJpaRepository
import com.agendly.tenant.infrastructure.persistence.repository.TenantJpaRepository
import com.agendly.tenant.infrastructure.persistence.repository.TenantSettingsJpaRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class MerchantAuthService(
  private val tenants: TenantJpaRepository,
  private val branding: TenantBrandingJpaRepository,
  private val settings: TenantSettingsJpaRepository,
  private val users: MerchantUserJpaRepository,
  private val encoder: PasswordEncoder,
  private val tokens: JwtTokensService
) {

  data class SignupCommand(
    val tenantName: String,
    val tenantSlug: String,
    val ownerName: String,
    val ownerEmail: String,
    val password: String
  )

  fun signup(cmd: SignupCommand): JwtTokens {
    if (tenants.findBySlug(cmd.tenantSlug) != null) {
      throw ConflictException("Slug already in use")
    }

    val tenant = TenantEntity().also {
      it.name = cmd.tenantName
      it.slug = cmd.tenantSlug
      it.status = "ACTIVE"
    }
    val savedTenant = tenants.save(tenant)

    branding.save(TenantBrandingEntity().also { b ->
      b.tenant = savedTenant
      b.primaryColor = "#111827"
      b.secondaryColor = "#F97316"
    })

    settings.save(TenantSettingsEntity().also { s ->
      s.tenant = savedTenant
      s.timezone = "America/Sao_Paulo"
      s.requireActiveSubscriptionToBook = true
    })

    val user = MerchantUserEntity().also { u ->
      u.tenant = savedTenant
      u.name = cmd.ownerName
      u.email = cmd.ownerEmail.lowercase()
      u.passwordHash = encoder.encode(cmd.password)
      u.role = "OWNER"
      u.status = "ACTIVE"
    }
    val savedUser = users.save(user)

    return tokens.issueForMerchant(
      merchantUserId = requireNotNull(savedUser.id),
      tenantId = requireNotNull(savedTenant.id),
      role = savedUser.role
    )
  }

  data class LoginCommand(
    val email: String,
    val password: String
  )

  fun login(cmd: LoginCommand): JwtTokens {
    val user = users.findByEmail(cmd.email.lowercase())
      ?: throw UnauthorizedException("Invalid credentials")

    val tenant = user.tenant

    if (!encoder.matches(cmd.password, user.passwordHash)) {
      throw UnauthorizedException("Invalid credentials")
    }

    return tokens.issueForMerchant(
      merchantUserId = requireNotNull(user.id),
      tenantId = requireNotNull(tenant.id),
      role = user.role
    )
  }
}
