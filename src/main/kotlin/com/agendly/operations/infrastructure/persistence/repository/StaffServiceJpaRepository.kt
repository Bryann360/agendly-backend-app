package com.agendly.operations.infrastructure.persistence.repository

import com.agendly.operations.infrastructure.persistence.entity.StaffServiceEntity
import com.agendly.operations.infrastructure.persistence.entity.StaffServiceId
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface StaffServiceJpaRepository : JpaRepository<StaffServiceEntity, StaffServiceId> {
  fun deleteAllByIdStaffId(staffId: UUID)
  fun existsByIdStaffIdAndIdServiceId(staffId: UUID, serviceId: UUID): Boolean
}
