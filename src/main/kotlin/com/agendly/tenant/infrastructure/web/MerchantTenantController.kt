package com.agendly.tenant.infrastructure.web

import com.agendly.shared.security.AuthContext
import com.agendly.tenant.infrastructure.persistence.repository.TenantBrandingJpaRepository
import com.agendly.tenant.infrastructure.persistence.repository.TenantJpaRepository
import jakarta.validation.constraints.Pattern
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("/api/merchant/tenant")
class MerchantTenantController(
  private val tenants: TenantJpaRepository,
  private val brandingRepo: TenantBrandingJpaRepository
) {

  data class TenantResponse(
    val id: String,
    val name: String,
    val slug: String,
    val status: String
  )

  data class BrandingResponse(
    val primaryColor: String,
    val secondaryColor: String,
    val logoKey: String?,
    val coverKey: String?
  )

  @GetMapping
  fun getTenant(): TenantResponse {
    val tenant = tenants.findById(AuthContext.tenantId()).orElseThrow()
    return TenantResponse(tenant.id.toString(), tenant.name, tenant.slug, tenant.status)
  }

  @GetMapping("/branding")
  fun getBranding(): BrandingResponse {
    val b = brandingRepo.findById(AuthContext.tenantId()).orElseThrow()
    return BrandingResponse(b.primaryColor, b.secondaryColor, b.logoKey, b.coverKey)
  }

  data class UpdateBrandingRequest(
    @field:Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "must be #RRGGBB")
    val primaryColor: String,
    @field:Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "must be #RRGGBB")
    val secondaryColor: String,
    val logoKey: String? = null,
    val coverKey: String? = null
  )

  @PutMapping("/branding")
  fun updateBranding(@RequestBody req: UpdateBrandingRequest): BrandingResponse {
    val tenantId = AuthContext.tenantId()
    val b = brandingRepo.findById(tenantId).orElseThrow()
    b.primaryColor = req.primaryColor
    b.secondaryColor = req.secondaryColor
    b.logoKey = req.logoKey
    b.coverKey = req.coverKey
    b.updatedAt = Instant.now()
    brandingRepo.save(b)
    return BrandingResponse(b.primaryColor, b.secondaryColor, b.logoKey, b.coverKey)
  }
}
