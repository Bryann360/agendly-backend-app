package com.agendly.shared.config

import com.agendly.billing.BillingProps
import com.agendly.shared.security.SecurityProps
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(SecurityProps::class, BillingProps::class)
class PropsConfig
