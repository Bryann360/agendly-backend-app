package com.agendly.catalog.infrastructure.web

import com.agendly.catalog.infrastructure.persistence.entity.PlanEntity
import com.agendly.catalog.infrastructure.persistence.entity.PlanServiceEntity
import com.agendly.catalog.infrastructure.persistence.entity.PlanServiceId
import com.agendly.catalog.infrastructure.persistence.repository.PlanJpaRepository
import com.agendly.catalog.infrastructure.persistence.repository.PlanServiceJpaRepository
import com.agendly.catalog.infrastructure.persistence.repository.ServiceJpaRepository
import com.agendly.shared.errors.exceptions.NotFoundException
import com.agendly.shared.security.AuthContext
import com.agendly.tenant.infrastructure.persistence.repository.TenantJpaRepository
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/merchant/plans")
class MerchantPlansController(
  private val plans: PlanJpaRepository,
  private val planServices: PlanServiceJpaRepository,
  private val services: ServiceJpaRepository,
  private val tenants: TenantJpaRepository
) {

  data class PlanResponse(
    val id: String,
    val name: String,
    val description: String?,
    val priceCents: Long,
    val billingCycle: String,
    val trialDays: Int,
    val maxBookingsPerCycle: Int?,
    val cooldownDays: Int,
    val active: Boolean,
    val allowedServiceIds: List<String>
  )

  @GetMapping
  fun list(): List<PlanResponse> {
    val tenantId = AuthContext.tenantId()
    return plans.findAllByTenantIdAndActiveTrue(tenantId).map { it.toResponse(tenantId) }
  }

  data class CreatePlanRequest(
    @field:NotBlank val name: String,
    val description: String? = null,
    @field:Min(0) val priceCents: Long,
    val billingCycle: String = "MONTHLY",
    @field:Min(0) val trialDays: Int = 0,
    val maxBookingsPerCycle: Int? = null,
    @field:Min(0) val cooldownDays: Int = 0,
    val allowedServiceIds: List<String> = emptyList()
  )

  @PostMapping
  fun create(@RequestBody req: CreatePlanRequest): PlanResponse {
    val tenant = tenants.findById(AuthContext.tenantId()).orElseThrow()
    val plan = PlanEntity().also {
      it.tenant = tenant
      it.name = req.name
      it.description = req.description
      it.priceCents = req.priceCents
      it.billingCycle = req.billingCycle
      it.trialDays = req.trialDays
      it.maxBookingsPerCycle = req.maxBookingsPerCycle
      it.cooldownDays = req.cooldownDays
      it.active = true
    }
    val saved = plans.save(plan)

    planServices.deleteAllByIdPlanId(requireNotNull(saved.id))
    req.allowedServiceIds.map(UUID::fromString).forEach { serviceId ->
      val service = services.findById(serviceId).orElseThrow { NotFoundException("Service not found: $serviceId") }
      if (service.tenant.id != tenant.id) throw NotFoundException("Service not found: $serviceId")
      planServices.save(PlanServiceEntity(PlanServiceId(requireNotNull(saved.id), serviceId)))
    }

    return saved.toResponse(requireNotNull(tenant.id))
  }

  data class UpdatePlanRequest(
    val name: String? = null,
    val description: String? = null,
    val priceCents: Long? = null,
    val billingCycle: String? = null,
    val trialDays: Int? = null,
    val maxBookingsPerCycle: Int? = null,
    val cooldownDays: Int? = null,
    val active: Boolean? = null,
    val allowedServiceIds: List<String>? = null
  )

  @PutMapping("/{id}")
  fun update(@PathVariable id: UUID, @RequestBody req: UpdatePlanRequest): PlanResponse {
    val tenantId = AuthContext.tenantId()
    val plan = plans.findByIdAndTenantId(id, tenantId) ?: throw NotFoundException("Plan not found")

    req.name?.let { plan.name = it }
    req.description?.let { plan.description = it }
    req.priceCents?.let { plan.priceCents = it }
    req.billingCycle?.let { plan.billingCycle = it }
    req.trialDays?.let { plan.trialDays = it }
    req.maxBookingsPerCycle?.let { plan.maxBookingsPerCycle = it }
    req.cooldownDays?.let { plan.cooldownDays = it }
    req.active?.let { plan.active = it }

    val saved = plans.save(plan)

    req.allowedServiceIds?.let { ids ->
      planServices.deleteAllByIdPlanId(id)
      ids.map(UUID::fromString).forEach { sid ->
        val service = services.findById(sid).orElseThrow { NotFoundException("Service not found: $sid") }
        if (service.tenant.id != tenantId) throw NotFoundException("Service not found: $sid")
        planServices.save(PlanServiceEntity(PlanServiceId(id, sid)))
      }
    }

    return saved.toResponse(tenantId)
  }

  private fun PlanEntity.toResponse(tenantId: UUID): PlanResponse {
    val allowed = planServices.findAllByIdPlanId(requireNotNull(id)).map { it.id.serviceId.toString() }
    return PlanResponse(
      id = requireNotNull(id).toString(),
      name = name,
      description = description,
      priceCents = priceCents,
      billingCycle = billingCycle,
      trialDays = trialDays,
      maxBookingsPerCycle = maxBookingsPerCycle,
      cooldownDays = cooldownDays,
      active = active,
      allowedServiceIds = allowed
    )
  }
}
