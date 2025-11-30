package com.agendly.merchant.infrastructure.web

import com.agendly.identity.infrastructure.persistence.repository.MerchantUserJpaRepository
import com.agendly.shared.security.AuthContext
import com.agendly.tenant.infrastructure.persistence.repository.TenantJpaRepository
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/merchant/auth/me")
class MerchantMeController(
  private val users: MerchantUserJpaRepository,
  private val tenants: TenantJpaRepository
) {
  data class MeResponse(
    val merchantUserId: String,
    val tenantId: String,
    val tenantSlug: String,
    val name: String,
    val email: String,
    val role: String
  )

  @GetMapping
  @PreAuthorize("hasAuthority('OWNER')")
  fun me(): MeResponse {
    val userId = AuthContext.merchantUserId()
    val tenantId = AuthContext.tenantId()
    val user = users.findById(userId).orElseThrow()
    val tenant = tenants.findById(tenantId).orElseThrow()
    return MeResponse(
      merchantUserId = userId.toString(),
      tenantId = tenantId.toString(),
      tenantSlug = tenant.slug,
      name = user.name,
      email = user.email,
      role = user.role
    )
  }
}
