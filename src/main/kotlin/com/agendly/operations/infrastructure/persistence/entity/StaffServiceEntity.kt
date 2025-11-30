package com.agendly.operations.infrastructure.persistence.entity

import jakarta.persistence.*
import java.io.Serializable
import java.util.UUID

@Embeddable
data class StaffServiceId(
  @Column(name = "staff_id") val staffId: UUID = UUID.randomUUID(),
  @Column(name = "service_id") val serviceId: UUID = UUID.randomUUID()
) : Serializable

@Entity
@Table(name = "staff_services")
class StaffServiceEntity(
  @EmbeddedId
  var id: StaffServiceId = StaffServiceId()
)
