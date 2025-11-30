package com.agendly.billing.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(
  name = "subscription_payments",
  indexes = [
    Index(name = "idx_subscription_payments_subscription_id", columnList = "subscription_id"),
    Index(name = "idx_subscription_payments_tenant_id", columnList = "tenant_id")
  ]
)
class SubscriptionPaymentEntity {

  @Id
  @Column(nullable = false)
  var id: UUID = UUID.randomUUID()

  @Column(name = "tenant_id", nullable = false)
  lateinit var tenantId: UUID

  @Column(name = "subscription_id", nullable = false)
  lateinit var subscriptionId: UUID

  @Column(nullable = false, length = 50)
  var provider: String = "INTERNAL"

  @Column(name = "provider_payment_id")
  var providerPaymentId: String? = null

  @Column(name = "amount_cents", nullable = false)
  var amountCents: Long = 0

  @Column(name = "platform_fee_cents", nullable = false)
  var platformFeeCents: Long = 0

  @Column(name = "merchant_fee_cents", nullable = false)
  var merchantFeeCents: Long = 0

  @Column(nullable = false, length = 30)
  var status: String = "PENDING"

  @Column(name = "paid_at")
  var paidAt: Instant? = null

  @Column(name = "failure_reason")
  var failureReason: String? = null

  @Column(name = "created_at", nullable = false)
  var createdAt: Instant = Instant.now()
}
