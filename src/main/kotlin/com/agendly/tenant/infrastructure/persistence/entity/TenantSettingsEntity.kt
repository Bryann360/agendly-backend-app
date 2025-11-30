package com.agendly.tenant.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "tenant_settings")
class TenantSettingsEntity {

  @Id
  @Column(name = "tenant_id", nullable = false)
  var tenantId: UUID? = null

  @MapsId
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tenant_id")
  var tenant: TenantEntity? = null

  @Column(nullable = false, length = 60)
  var timezone: String = "America/Sao_Paulo"

  @Column(nullable = false, length = 10)
  var currency: String = "BRL"

  @Column(nullable = false, length = 10)
  var locale: String = "pt-BR"

  @Column(name = "require_active_subscription_to_book", nullable = false)
  var requireActiveSubscriptionToBook: Boolean = true

  @Column(name = "booking_window_days", nullable = false)
  var bookingWindowDays: Int = 60

  @Column(name = "cancel_min_notice_hours", nullable = false)
  var cancelMinNoticeHours: Int = 6

  @Column(name = "max_active_bookings_per_client", nullable = false)
  var maxActiveBookingsPerClient: Int = 3

  @Column(name = "updated_at", nullable = false)
  var updatedAt: Instant = Instant.now()
}
