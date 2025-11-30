package com.agendly.client.infrastructure.persistence.repository

import com.agendly.client.infrastructure.persistence.entity.ClientAccountEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ClientAccountJpaRepository : JpaRepository<ClientAccountEntity, UUID> {
  fun findByEmail(email: String): ClientAccountEntity?
}
