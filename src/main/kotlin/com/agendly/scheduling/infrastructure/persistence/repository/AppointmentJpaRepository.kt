package com.agendly.scheduling.infrastructure.persistence.repository

import com.agendly.scheduling.infrastructure.persistence.entity.AppointmentEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant
import java.util.UUID

interface AppointmentJpaRepository : JpaRepository<AppointmentEntity, UUID> {
  fun existsByStaffIdAndStartAtLessThanAndEndAtGreaterThan(staffId: UUID, endExclusive: Instant, startExclusive: Instant): Boolean
  fun findAllByStaffIdAndStartAtBetween(staffId: UUID, from: Instant, to: Instant): List<AppointmentEntity>
}
