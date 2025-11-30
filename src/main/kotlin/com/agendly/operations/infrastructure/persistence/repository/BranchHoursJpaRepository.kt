package com.agendly.operations.infrastructure.persistence.repository

import com.agendly.operations.infrastructure.persistence.entity.BranchHoursEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface BranchHoursJpaRepository : JpaRepository<BranchHoursEntity, UUID> {
  fun findAllByBranchIdAndDayOfWeek(branchId: UUID, dayOfWeek: Int): List<BranchHoursEntity>
}
