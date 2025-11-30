package com.agendly.shared.persistence

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.UUID

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseJpaEntity {

  @Id
  @UuidGenerator
  @Column(nullable = false, updatable = false)
  var id: UUID? = null

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  var createdAt: Instant? = null

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  var updatedAt: Instant? = null
}
