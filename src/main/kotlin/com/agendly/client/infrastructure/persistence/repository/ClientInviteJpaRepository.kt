package com.agendly.client.infrastructure.persistence.repository

import com.agendly.client.infrastructure.persistence.entity.ClientInviteEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ClientInviteJpaRepository : JpaRepository<ClientInviteEntity, UUID> {
  fun findByTokenHash(tokenHash: String): ClientInviteEntity?
}
