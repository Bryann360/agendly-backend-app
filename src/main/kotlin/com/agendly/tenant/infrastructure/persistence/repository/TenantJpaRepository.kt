package com.agendly.tenant.infrastructure.persistence.repository

import com.agendly.tenant.infrastructure.persistence.entity.TenantEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TenantJpaRepository : JpaRepository<TenantEntity, UUID> {
  fun findBySlug(slug: String): TenantEntity?
}
