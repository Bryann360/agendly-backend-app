package com.agendly.operations.infrastructure.persistence.repository

import com.agendly.operations.infrastructure.persistence.entity.StaffWorkingHoursEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface StaffWorkingHoursJpaRepository : JpaRepository<StaffWorkingHoursEntity, UUID> {
  fun findAllByStaffIdAndDayOfWeek(staffId: UUID, dayOfWeek: Int): List<StaffWorkingHoursEntity>
}
