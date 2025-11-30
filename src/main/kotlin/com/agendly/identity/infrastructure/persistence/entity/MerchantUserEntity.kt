package com.agendly.identity.infrastructure.persistence.entity

import com.agendly.shared.persistence.BaseJpaEntity
import com.agendly.tenant.infrastructure.persistence.entity.TenantEntity
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
  name = "merchant_users",
  uniqueConstraints = [UniqueConstraint(name = "uk_merchant_users_tenant_email", columnNames = ["tenant_id","email"])],
  indexes = [Index(name = "idx_merchant_users_tenant_id", columnList = "tenant_id")]
)
class MerchantUserEntity : BaseJpaEntity() {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tenant_id", nullable = false)
  lateinit var tenant: TenantEntity

  @Column(nullable = false, length = 150)
  lateinit var name: String

  @Column(nullable = false, length = 150)
  lateinit var email: String

  @Column(name = "password_hash", nullable = false)
  lateinit var passwordHash: String

  @Column(nullable = false, length = 30)
  var role: String = "OWNER"

  @Column(nullable = false, length = 30)
  var status: String = "ACTIVE"

  @Column(name = "last_login_at")
  var lastLoginAt: Instant? = null
}
