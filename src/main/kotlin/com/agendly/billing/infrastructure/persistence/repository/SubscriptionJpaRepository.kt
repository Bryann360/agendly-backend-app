package com.agendly.billing.infrastructure.persistence.repository

import com.agendly.billing.infrastructure.persistence.entity.SubscriptionEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SubscriptionJpaRepository : JpaRepository<SubscriptionEntity, UUID> {
  fun findByTenantIdAndTenantClientId(tenantId: UUID, tenantClientId: UUID): SubscriptionEntity?
  fun findByIdAndTenantId(id: UUID, tenantId: UUID): SubscriptionEntity?
}
