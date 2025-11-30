package com.agendly.publicapi.infrastructure.web

import com.agendly.scheduling.application.SchedulingService
import jakarta.validation.constraints.NotBlank
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/api/public/{tenantSlug}/scheduling")
@Validated
class PublicSchedulingController(
  private val scheduling: SchedulingService
) {

  data class TimeSlotResponse(val start: Instant, val end: Instant, val available: Boolean)

  @GetMapping("/availability")
  fun availability(
    @PathVariable tenantSlug: String,
    @RequestParam serviceId: UUID,
    @RequestParam staffId: UUID,
    @RequestParam date: LocalDate
  ): List<TimeSlotResponse> =
    scheduling.availability(tenantSlug, serviceId, staffId, date).map { TimeSlotResponse(it.start, it.end, it.available) }

  data class CreateAppointmentRequest(
    val tenantClientId: UUID,
    val staffId: UUID,
    val serviceId: UUID,
    val startAt: Instant,
    val notes: String? = null
  )

  @PostMapping("/appointments")
  fun createAppointment(
    @PathVariable tenantSlug: String,
    @RequestBody req: CreateAppointmentRequest
  ): SchedulingService.AppointmentResult =
    scheduling.createAppointment(
      SchedulingService.CreateAppointmentCommand(
        tenantSlug = tenantSlug,
        tenantClientId = req.tenantClientId,
        staffId = req.staffId,
        serviceId = req.serviceId,
        startAt = req.startAt,
        notes = req.notes
      )
    )
}
