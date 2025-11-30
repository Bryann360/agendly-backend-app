package com.agendly.publicapi.infrastructure.web

import com.agendly.catalog.infrastructure.persistence.repository.PlanJpaRepository
import com.agendly.catalog.infrastructure.persistence.repository.PlanServiceJpaRepository
import com.agendly.catalog.infrastructure.persistence.repository.ServiceJpaRepository
import com.agendly.operations.infrastructure.persistence.repository.StaffMemberJpaRepository
import com.agendly.publicapi.application.PublicTenantResolver
import com.agendly.tenant.infrastructure.persistence.repository.TenantBrandingJpaRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/public/{tenantSlug}")
class PublicTenantController(
  private val resolver: PublicTenantResolver,
  private val branding: TenantBrandingJpaRepository,
  private val services: ServiceJpaRepository,
  private val staff: StaffMemberJpaRepository,
  private val plans: PlanJpaRepository,
  private val planServices: PlanServiceJpaRepository
) {

  data class BrandingResponse(
    val tenantName: String,
    val tenantSlug: String,
    val primaryColor: String,
    val secondaryColor: String,
    val logoKey: String?,
    val coverKey: String?
  )

  @GetMapping("/branding")
  fun branding(@PathVariable tenantSlug: String): BrandingResponse {
    val tenant = resolver.bySlugOrThrow(tenantSlug)
    val b = branding.findById(tenant.id!!).orElseThrow()
    return BrandingResponse(
      tenantName = tenant.name,
      tenantSlug = tenant.slug,
      primaryColor = b.primaryColor,
      secondaryColor = b.secondaryColor,
      logoKey = b.logoKey,
      coverKey = b.coverKey
    )
  }

  data class ServiceResponse(
    val id: String, val name: String, val description: String?, val durationMin: Int
  )

  @GetMapping("/services")
  fun services(@PathVariable tenantSlug: String): List<ServiceResponse> {
    val tenant = resolver.bySlugOrThrow(tenantSlug)
    return services.findAllByTenantIdAndActiveTrue(tenant.id!!).map {
      ServiceResponse(it.id.toString(), it.name, it.description, it.durationMin)
    }
  }

  data class StaffResponse(
    val id: String, val name: String, val bio: String?
  )

  @GetMapping("/staff")
  fun staff(@PathVariable tenantSlug: String): List<StaffResponse> {
    val tenant = resolver.bySlugOrThrow(tenantSlug)
    return staff.findAllByTenantIdAndActiveTrue(tenant.id!!).map {
      StaffResponse(it.id.toString(), it.name, it.bio)
    }
  }

  data class PlanResponse(
    val id: String,
    val name: String,
    val description: String?,
    val priceCents: Long,
    val billingCycle: String,
    val maxBookingsPerCycle: Int?,
    val allowedServiceIds: List<String>
  )

  @GetMapping("/plans")
  fun plans(@PathVariable tenantSlug: String): List<PlanResponse> {
    val tenant = resolver.bySlugOrThrow(tenantSlug)
    return plans.findAllByTenantIdAndActiveTrue(tenant.id!!).map { p ->
      val allowed = planServices.findAllByIdPlanId(p.id!!).map { it.id.serviceId.toString() }
      PlanResponse(
        id = p.id.toString(),
        name = p.name,
        description = p.description,
        priceCents = p.priceCents,
        billingCycle = p.billingCycle,
        maxBookingsPerCycle = p.maxBookingsPerCycle,
        allowedServiceIds = allowed
      )
    }
  }
}
