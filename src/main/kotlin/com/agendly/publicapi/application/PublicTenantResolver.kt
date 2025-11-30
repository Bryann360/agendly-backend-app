package com.agendly.publicapi.application

import com.agendly.shared.errors.exceptions.NotFoundException
import com.agendly.tenant.infrastructure.persistence.entity.TenantEntity
import com.agendly.tenant.infrastructure.persistence.repository.TenantJpaRepository
import org.springframework.stereotype.Component

@Component
class PublicTenantResolver(
  private val tenants: TenantJpaRepository
) {
  fun bySlugOrThrow(slug: String): TenantEntity =
    tenants.findBySlug(slug) ?: throw NotFoundException("Tenant not found")
}
