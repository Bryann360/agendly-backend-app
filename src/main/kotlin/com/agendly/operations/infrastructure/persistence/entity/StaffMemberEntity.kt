package com.agendly.operations.infrastructure.persistence.entity

import com.agendly.shared.persistence.BaseJpaEntity
import com.agendly.tenant.infrastructure.persistence.entity.TenantEntity
import jakarta.persistence.*

@Entity
@Table(
  name = "staff_members",
  indexes = [
    Index(name = "idx_staff_members_tenant_id", columnList = "tenant_id"),
    Index(name = "idx_staff_members_branch_id", columnList = "branch_id")
  ]
)
class StaffMemberEntity : BaseJpaEntity() {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tenant_id", nullable = false)
  lateinit var tenant: TenantEntity

  @Column(name = "branch_id")
  var branchId: java.util.UUID? = null

  @Column(nullable = false, length = 150)
  lateinit var name: String

  var bio: String? = null
  var phone: String? = null

  @Column(nullable = false)
  var active: Boolean = true
}
