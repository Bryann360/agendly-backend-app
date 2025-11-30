package com.agendly.operations.infrastructure.persistence.repository

import com.agendly.operations.infrastructure.persistence.entity.BranchEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface BranchJpaRepository : JpaRepository<BranchEntity, UUID> {
  fun findAllByTenantIdAndActiveTrue(tenantId: UUID): List<BranchEntity>
}
