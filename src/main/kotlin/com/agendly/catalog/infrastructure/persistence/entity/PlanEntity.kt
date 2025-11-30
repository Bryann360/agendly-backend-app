package com.agendly.catalog.infrastructure.persistence.entity

import com.agendly.shared.persistence.BaseJpaEntity
import com.agendly.tenant.infrastructure.persistence.entity.TenantEntity
import jakarta.persistence.*

@Entity
@Table(
  name = "plans",
  uniqueConstraints = [UniqueConstraint(name = "uk_plans_tenant_name", columnNames = ["tenant_id","name"])],
  indexes = [Index(name = "idx_plans_tenant_id", columnList = "tenant_id")]
)
class PlanEntity : BaseJpaEntity() {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tenant_id", nullable = false)
  lateinit var tenant: TenantEntity

  @Column(nullable = false, length = 120)
  lateinit var name: String

  var description: String? = null

  @Column(name = "price_cents", nullable = false)
  var priceCents: Long = 0

  @Column(name = "billing_cycle", nullable = false, length = 20)
  var billingCycle: String = "MONTHLY"

  @Column(name = "trial_days", nullable = false)
  var trialDays: Int = 0

  @Column(name = "max_bookings_per_cycle")
  var maxBookingsPerCycle: Int? = null

  @Column(name = "cooldown_days", nullable = false)
  var cooldownDays: Int = 0

  @Column(nullable = false)
  var active: Boolean = true
}
