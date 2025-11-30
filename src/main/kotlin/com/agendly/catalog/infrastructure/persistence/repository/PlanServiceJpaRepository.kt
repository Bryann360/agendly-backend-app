package com.agendly.catalog.infrastructure.persistence.repository

import com.agendly.catalog.infrastructure.persistence.entity.PlanServiceEntity
import com.agendly.catalog.infrastructure.persistence.entity.PlanServiceId
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PlanServiceJpaRepository : JpaRepository<PlanServiceEntity, PlanServiceId> {
  fun deleteAllByIdPlanId(planId: UUID)
  fun findAllByIdPlanId(planId: UUID): List<PlanServiceEntity>
  fun existsByIdPlanIdAndIdServiceId(planId: UUID, serviceId: UUID): Boolean
}
