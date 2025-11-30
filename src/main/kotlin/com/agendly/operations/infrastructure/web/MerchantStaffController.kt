package com.agendly.operations.infrastructure.web

import com.agendly.catalog.infrastructure.persistence.repository.ServiceJpaRepository
import com.agendly.operations.infrastructure.persistence.entity.StaffMemberEntity
import com.agendly.operations.infrastructure.persistence.entity.StaffServiceEntity
import com.agendly.operations.infrastructure.persistence.entity.StaffServiceId
import com.agendly.operations.infrastructure.persistence.repository.StaffMemberJpaRepository
import com.agendly.operations.infrastructure.persistence.repository.StaffServiceJpaRepository
import com.agendly.shared.errors.exceptions.NotFoundException
import com.agendly.shared.security.AuthContext
import com.agendly.tenant.infrastructure.persistence.repository.TenantJpaRepository
import jakarta.validation.constraints.NotBlank
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/merchant/staff")
class MerchantStaffController(
  private val staffRepo: StaffMemberJpaRepository,
  private val staffServices: StaffServiceJpaRepository,
  private val services: ServiceJpaRepository,
  private val tenants: TenantJpaRepository
) {

  data class StaffResponse(
    val id: String,
    val name: String,
    val bio: String?,
    val phone: String?,
    val branchId: String?,
    val active: Boolean,
    val serviceIds: List<String>
  )

  @GetMapping
  fun list(): List<StaffResponse> {
    val tenantId = AuthContext.tenantId()
    return staffRepo.findAllByTenantIdAndActiveTrue(tenantId).map { it.toResponse() }
  }

  data class CreateStaffRequest(
    @field:NotBlank val name: String,
    val bio: String? = null,
    val phone: String? = null,
    val branchId: String? = null,
    val serviceIds: List<String> = emptyList()
  )

  @PostMapping
  fun create(@RequestBody req: CreateStaffRequest): StaffResponse {
    val tenant = tenants.findById(AuthContext.tenantId()).orElseThrow()
    val staff = StaffMemberEntity().also {
      it.tenant = tenant
      it.name = req.name
      it.bio = req.bio
      it.phone = req.phone
      it.branchId = req.branchId?.let(UUID::fromString)
      it.active = true
    }
    val saved = staffRepo.save(staff)

    staffServices.deleteAllByIdStaffId(requireNotNull(saved.id))
    req.serviceIds.map(UUID::fromString).forEach { sid ->
      val service = services.findById(sid).orElseThrow { NotFoundException("Service not found: $sid") }
      if (service.tenant.id != tenant.id) throw NotFoundException("Service not found: $sid")
      staffServices.save(StaffServiceEntity(StaffServiceId(requireNotNull(saved.id), sid)))
    }

    return saved.toResponse()
  }

  data class UpdateStaffRequest(
    val name: String? = null,
    val bio: String? = null,
    val phone: String? = null,
    val branchId: String? = null,
    val active: Boolean? = null,
    val serviceIds: List<String>? = null
  )

  @PutMapping("/{id}")
  fun update(@PathVariable id: UUID, @RequestBody req: UpdateStaffRequest): StaffResponse {
    val tenantId = AuthContext.tenantId()
    val staff = staffRepo.findByIdAndTenantId(id, tenantId) ?: throw NotFoundException("Staff not found")

    req.name?.let { staff.name = it }
    req.bio?.let { staff.bio = it }
    req.phone?.let { staff.phone = it }
    req.branchId?.let { staff.branchId = UUID.fromString(it) }
    req.active?.let { staff.active = it }

    val saved = staffRepo.save(staff)

    req.serviceIds?.let { ids ->
      staffServices.deleteAllByIdStaffId(id)
      ids.map(UUID::fromString).forEach { sid ->
        val service = services.findById(sid).orElseThrow { NotFoundException("Service not found: $sid") }
        if (service.tenant.id != tenantId) throw NotFoundException("Service not found: $sid")
        staffServices.save(StaffServiceEntity(StaffServiceId(id, sid)))
      }
    }

    return saved.toResponse()
  }

  private fun StaffMemberEntity.toResponse(): StaffResponse {
    val sid = requireNotNull(id)
    val mapped = staffServices.findAll().filter { it.id.staffId == sid }.map { it.id.serviceId.toString() }
    return StaffResponse(
      id = sid.toString(),
      name = name,
      bio = bio,
      phone = phone,
      branchId = branchId?.toString(),
      active = active,
      serviceIds = mapped
    )
  }
}
