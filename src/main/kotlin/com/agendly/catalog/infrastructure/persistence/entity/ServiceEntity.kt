package com.agendly.catalog.infrastructure.persistence.entity

import com.agendly.shared.persistence.BaseJpaEntity
import com.agendly.tenant.infrastructure.persistence.entity.TenantEntity
import jakarta.persistence.*

@Entity
@Table(
  name = "services",
  indexes = [Index(name = "idx_services_tenant_id", columnList = "tenant_id")]
)
class ServiceEntity : BaseJpaEntity() {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tenant_id", nullable = false)
  lateinit var tenant: TenantEntity

  @Column(nullable = false, length = 150)
  lateinit var name: String

  var description: String? = null

  @Column(name = "duration_min", nullable = false)
  var durationMin: Int = 30

  @Column(nullable = false)
  var active: Boolean = true
}
