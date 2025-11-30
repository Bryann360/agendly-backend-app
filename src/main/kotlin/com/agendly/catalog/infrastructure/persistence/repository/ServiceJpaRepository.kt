package com.agendly.catalog.infrastructure.persistence.repository

import com.agendly.catalog.infrastructure.persistence.entity.ServiceEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ServiceJpaRepository : JpaRepository<ServiceEntity, UUID> {
  fun findAllByTenantIdAndActiveTrue(tenantId: UUID): List<ServiceEntity>
}
