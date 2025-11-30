package com.agendly.client.infrastructure.persistence.entity

import com.agendly.shared.persistence.BaseJpaEntity
import jakarta.persistence.*

@Entity
@Table(
  name = "client_accounts",
  uniqueConstraints = [UniqueConstraint(name = "uk_client_accounts_email", columnNames = ["email"])]
)
class ClientAccountEntity : BaseJpaEntity() {

  @Column(nullable = false, length = 150)
  lateinit var name: String

  @Column(length = 150)
  var email: String? = null

  @Column(length = 30)
  var phone: String? = null
}
