package com.agendly.catalog.infrastructure.web

import com.agendly.catalog.infrastructure.persistence.entity.ServiceEntity
import com.agendly.catalog.infrastructure.persistence.repository.ServiceJpaRepository
import com.agendly.shared.errors.exceptions.NotFoundException
import com.agendly.shared.security.AuthContext
import com.agendly.tenant.infrastructure.persistence.repository.TenantJpaRepository
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/merchant/services")
class MerchantServicesController(
  private val services: ServiceJpaRepository,
  private val tenants: TenantJpaRepository
) {

  data class ServiceResponse(
    val id: String,
    val name: String,
    val description: String?,
    val durationMin: Int,
    val active: Boolean
  )

  @GetMapping
  fun list(): List<ServiceResponse> =
    services.findAllByTenantIdAndActiveTrue(AuthContext.tenantId()).map { it.toResponse() }

  data class CreateServiceRequest(
    @field:NotBlank val name: String,
    val description: String? = null,
    @field:Min(5) val durationMin: Int
  )

  @PostMapping
  fun create(@RequestBody req: CreateServiceRequest): ServiceResponse {
    val tenant = tenants.findById(AuthContext.tenantId()).orElseThrow()
    val entity = ServiceEntity().also {
      it.tenant = tenant
      it.name = req.name
      it.description = req.description
      it.durationMin = req.durationMin
      it.active = true
    }
    return services.save(entity).toResponse()
  }

  data class UpdateServiceRequest(
    val name: String? = null,
    val description: String? = null,
    val durationMin: Int? = null,
    val active: Boolean? = null
  )

  @PutMapping("/{id}")
  fun update(@PathVariable id: UUID, @RequestBody req: UpdateServiceRequest): ServiceResponse {
    val tenantId = AuthContext.tenantId()
    val e = services.findById(id).orElseThrow { NotFoundException("Service not found") }
    if (e.tenant.id != tenantId) throw NotFoundException("Service not found")

    req.name?.let { e.name = it }
    req.description?.let { e.description = it }
    req.durationMin?.let { e.durationMin = it }
    req.active?.let { e.active = it }

    return services.save(e).toResponse()
  }

  @DeleteMapping("/{id}")
  fun delete(@PathVariable id: UUID) {
    val tenantId = AuthContext.tenantId()
    val e = services.findById(id).orElseThrow { NotFoundException("Service not found") }
    if (e.tenant.id != tenantId) throw NotFoundException("Service not found")
    e.active = false
    services.save(e)
  }

  private fun ServiceEntity.toResponse() = ServiceResponse(
    id = requireNotNull(id).toString(),
    name = name,
    description = description,
    durationMin = durationMin,
    active = active
  )
}
