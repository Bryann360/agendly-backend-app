package com.agendly.billing.infrastructure.persistence.repository

import com.agendly.billing.infrastructure.persistence.entity.SubscriptionPaymentEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SubscriptionPaymentJpaRepository : JpaRepository<SubscriptionPaymentEntity, UUID> {
  fun findAllBySubscriptionId(subscriptionId: UUID): List<SubscriptionPaymentEntity>
}
