package com.agendly.operations.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.Instant
import java.time.LocalTime
import java.util.UUID

@Entity
@Table(
  name = "branch_hours",
  uniqueConstraints = [UniqueConstraint(name = "uk_branch_hours_branch_day", columnNames = ["branch_id","day_of_week"])],
  indexes = [Index(name = "idx_branch_hours_branch_id", columnList = "branch_id")]
)
class BranchHoursEntity {

  @Id
  @Column(nullable = false)
  var id: UUID = UUID.randomUUID()

  @Column(name = "branch_id", nullable = false)
  lateinit var branchId: UUID

  @Column(name = "day_of_week", nullable = false)
  var dayOfWeek: Int = 1

  @Column(name = "open_time", nullable = false)
  lateinit var openTime: LocalTime

  @Column(name = "close_time", nullable = false)
  lateinit var closeTime: LocalTime

  @Column(name = "created_at", nullable = false)
  var createdAt: Instant = Instant.now()
}
