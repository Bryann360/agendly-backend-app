package com.agendly.operations.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.Instant
import java.time.LocalTime
import java.util.UUID

@Entity
@Table(
  name = "staff_working_hours",
  indexes = [Index(name = "idx_staff_working_hours_staff_id", columnList = "staff_id")]
)
class StaffWorkingHoursEntity {

  @Id
  @Column(nullable = false)
  var id: UUID = UUID.randomUUID()

  @Column(name = "staff_id", nullable = false)
  lateinit var staffId: UUID

  @Column(name = "day_of_week", nullable = false)
  var dayOfWeek: Int = 1

  @Column(name = "start_time", nullable = false)
  lateinit var startTime: LocalTime

  @Column(name = "end_time", nullable = false)
  lateinit var endTime: LocalTime

  @Column(name = "branch_id")
  var branchId: UUID? = null

  @Column(name = "created_at", nullable = false)
  var createdAt: Instant = Instant.now()
}
