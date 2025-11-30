package com.agendly.tenant.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "tenant_branding")
class TenantBrandingEntity {

  @Id
  @Column(name = "tenant_id", nullable = false)
  var tenantId: UUID? = null

  @MapsId
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tenant_id")
  var tenant: TenantEntity? = null

  @Column(name = "primary_color", nullable = false, length = 9)
  var primaryColor: String = "#111827"

  @Column(name = "secondary_color", nullable = false, length = 9)
  var secondaryColor: String = "#F97316"

  @Column(name = "logo_key")
  var logoKey: String? = null

  @Column(name = "cover_key")
  var coverKey: String? = null

  @Column(name = "updated_at", nullable = false)
  var updatedAt: Instant = Instant.now()
}
