package com.agendly.client.infrastructure.persistence.entity

import com.agendly.shared.persistence.BaseJpaEntity
import com.agendly.identity.infrastructure.persistence.entity.MerchantUserEntity
import com.agendly.tenant.infrastructure.persistence.entity.TenantEntity
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
  name = "tenant_clients",
  uniqueConstraints = [UniqueConstraint(name = "uk_tenant_clients_tenant_client", columnNames = ["tenant_id","client_account_id"])],
  indexes = [
    Index(name = "idx_tenant_clients_tenant_id", columnList = "tenant_id"),
    Index(name = "idx_tenant_clients_client_id", columnList = "client_account_id")
  ]
)
class TenantClientEntity : BaseJpaEntity() {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tenant_id", nullable = false)
  lateinit var tenant: TenantEntity

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "client_account_id", nullable = false)
  lateinit var clientAccount: ClientAccountEntity

  @Column(nullable = false, length = 30)
  var status: String = "ACTIVE"

  @Column(name = "joined_at", nullable = false)
  var joinedAt: Instant = Instant.now()

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "invited_by_user_id")
  var invitedBy: MerchantUserEntity? = null
}
