package com.agendly.catalog.infrastructure.persistence.repository

import com.agendly.catalog.infrastructure.persistence.entity.PlanEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PlanJpaRepository : JpaRepository<PlanEntity, UUID> {
  fun findAllByTenantIdAndActiveTrue(tenantId: UUID): List<PlanEntity>
  fun findByIdAndTenantId(id: UUID, tenantId: UUID): PlanEntity?
}
