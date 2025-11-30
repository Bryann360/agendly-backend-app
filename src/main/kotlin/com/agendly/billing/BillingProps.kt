package com.agendly.billing

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "agendly.billing")
data class BillingProps(
  val platformFeeBps: Int,
  val merchantDefaultTrialDays: Int,
  val allowFreeSubscriptions: Boolean
)
