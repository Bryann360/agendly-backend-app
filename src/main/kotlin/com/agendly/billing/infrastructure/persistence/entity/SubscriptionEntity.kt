package com.agendly.billing.infrastructure.persistence.entity

import com.agendly.shared.persistence.BaseJpaEntity
import com.agendly.catalog.infrastructure.persistence.entity.PlanEntity
import com.agendly.tenant.infrastructure.persistence.entity.TenantEntity
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(
  name = "subscriptions",
  uniqueConstraints = [UniqueConstraint(name = "uk_subscriptions_tenant_client", columnNames = ["tenant_id","tenant_client_id"])],
  indexes = [
    Index(name = "idx_subscriptions_tenant_id", columnList = "tenant_id"),
    Index(name = "idx_subscriptions_plan_id", columnList = "plan_id")
  ]
)
class SubscriptionEntity : BaseJpaEntity() {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tenant_id", nullable = false)
  lateinit var tenant: TenantEntity

  @Column(name = "tenant_client_id", nullable = false)
  lateinit var tenantClientId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "plan_id", nullable = false)
  lateinit var plan: PlanEntity

  @Column(nullable = false, length = 30)
  var status: String = "PENDING"

  @Column(name = "started_at")
  var startedAt: Instant? = null

  @Column(name = "current_period_start")
  var currentPeriodStart: Instant? = null

  @Column(name = "current_period_end")
  var currentPeriodEnd: Instant? = null

  @Column(name = "cancel_at_period_end", nullable = false)
  var cancelAtPeriodEnd: Boolean = false

  @Column(name = "canceled_at")
  var canceledAt: Instant? = null

  @Column(name = "next_billing_at")
  var nextBillingAt: Instant? = null

  @Column(name = "remaining_credits")
  var remainingCredits: Int? = null
}
