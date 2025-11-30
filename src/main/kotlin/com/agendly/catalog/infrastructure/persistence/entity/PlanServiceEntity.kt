package com.agendly.catalog.infrastructure.persistence.entity

import jakarta.persistence.*
import java.io.Serializable
import java.util.UUID

@Embeddable
data class PlanServiceId(
  @Column(name = "plan_id") val planId: UUID = UUID.randomUUID(),
  @Column(name = "service_id") val serviceId: UUID = UUID.randomUUID()
) : Serializable

@Entity
@Table(name = "plan_services")
class PlanServiceEntity(
  @EmbeddedId
  var id: PlanServiceId = PlanServiceId()
)
