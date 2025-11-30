package com.agendly.client.infrastructure.persistence.repository

import com.agendly.client.infrastructure.persistence.entity.TenantClientEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TenantClientJpaRepository : JpaRepository<TenantClientEntity, UUID> {
  fun findByTenantIdAndClientAccountId(tenantId: UUID, clientAccountId: UUID): TenantClientEntity?
  fun findAllByTenantId(tenantId: UUID): List<TenantClientEntity>
}
