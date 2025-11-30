package com.agendly.tenant.infrastructure.persistence.repository

import com.agendly.tenant.infrastructure.persistence.entity.TenantBrandingEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TenantBrandingJpaRepository : JpaRepository<TenantBrandingEntity, UUID>
