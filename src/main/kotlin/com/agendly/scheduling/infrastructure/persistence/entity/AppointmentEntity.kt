package com.agendly.scheduling.infrastructure.persistence.entity

import com.agendly.shared.persistence.BaseJpaEntity
import com.agendly.tenant.infrastructure.persistence.entity.TenantEntity
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(
  name = "appointments",
  indexes = [
    Index(name = "idx_appointments_tenant_start", columnList = "tenant_id,start_at"),
    Index(name = "idx_appointments_staff_start", columnList = "staff_id,start_at"),
    Index(name = "idx_appointments_tenant_client", columnList = "tenant_client_id")
  ]
)
class AppointmentEntity : BaseJpaEntity() {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tenant_id", nullable = false)
  lateinit var tenant: TenantEntity

  @Column(name = "tenant_client_id", nullable = false)
  lateinit var tenantClientId: UUID

  @Column(name = "staff_id", nullable = false)
  lateinit var staffId: UUID

  @Column(name = "branch_id")
  var branchId: UUID? = null

  @Column(name = "service_id", nullable = false)
  lateinit var serviceId: UUID

  @Column(name = "subscription_id")
  var subscriptionId: UUID? = null

  @Column(name = "start_at", nullable = false)
  lateinit var startAt: Instant

  @Column(name = "end_at", nullable = false)
  lateinit var endAt: Instant

  @Column(nullable = false, length = 30)
  var status: String = "CONFIRMED"

  var notes: String? = null

  @Column(name = "cancel_reason")
  var cancelReason: String? = null
}
