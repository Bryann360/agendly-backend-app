package com.agendly.client.infrastructure.persistence.entity

import com.agendly.catalog.infrastructure.persistence.entity.PlanEntity
import com.agendly.identity.infrastructure.persistence.entity.MerchantUserEntity
import com.agendly.tenant.infrastructure.persistence.entity.TenantEntity
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(
  name = "client_invites",
  uniqueConstraints = [UniqueConstraint(name = "uk_client_invites_token_hash", columnNames = ["token_hash"])],
  indexes = [Index(name = "idx_client_invites_tenant_id", columnList = "tenant_id")]
)
class ClientInviteEntity {

  @Id
  @Column(nullable = false)
  var id: UUID = UUID.randomUUID()

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tenant_id", nullable = false)
  lateinit var tenant: TenantEntity

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by_user_id", nullable = false)
  lateinit var createdBy: MerchantUserEntity

  @Column(name = "token_hash", nullable = false)
  lateinit var tokenHash: String

  @Column(name = "expires_at")
  var expiresAt: Instant? = null

  @Column(name = "max_uses", nullable = false)
  var maxUses: Int = 1

  @Column(name = "used_count", nullable = false)
  var usedCount: Int = 0

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "preselected_plan_id")
  var preselectedPlan: PlanEntity? = null

  @Column(name = "created_at", nullable = false)
  var createdAt: Instant = Instant.now()
}
