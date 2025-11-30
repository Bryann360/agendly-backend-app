package com.agendly.tenant.infrastructure.persistence.entity

import com.agendly.shared.persistence.BaseJpaEntity
import jakarta.persistence.*

@Entity
@Table(
  name = "tenants",
  indexes = [Index(name = "idx_tenants_slug", columnList = "slug", unique = true)]
)
class TenantEntity : BaseJpaEntity() {

  @Column(nullable = false, length = 150)
  lateinit var name: String

  @Column(nullable = false, length = 80, unique = true)
  lateinit var slug: String

  @Column(nullable = false, length = 30)
  var status: String = "ACTIVE"
}
