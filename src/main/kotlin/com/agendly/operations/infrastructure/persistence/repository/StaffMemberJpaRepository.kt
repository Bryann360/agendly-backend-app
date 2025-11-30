package com.agendly.operations.infrastructure.persistence.repository

import com.agendly.operations.infrastructure.persistence.entity.StaffMemberEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface StaffMemberJpaRepository : JpaRepository<StaffMemberEntity, UUID> {
  fun findAllByTenantIdAndActiveTrue(tenantId: UUID): List<StaffMemberEntity>
  fun findByIdAndTenantId(id: UUID, tenantId: UUID): StaffMemberEntity?
}
