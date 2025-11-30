package com.agendly.scheduling.application

import com.agendly.billing.infrastructure.persistence.repository.SubscriptionJpaRepository
import com.agendly.catalog.infrastructure.persistence.repository.PlanServiceJpaRepository
import com.agendly.catalog.infrastructure.persistence.repository.ServiceJpaRepository
import com.agendly.client.infrastructure.persistence.repository.TenantClientJpaRepository
import com.agendly.operations.infrastructure.persistence.repository.StaffMemberJpaRepository
import com.agendly.operations.infrastructure.persistence.repository.StaffWorkingHoursJpaRepository
import com.agendly.shared.errors.exceptions.BadRequestException
import com.agendly.shared.errors.exceptions.NotFoundException
import com.agendly.scheduling.infrastructure.persistence.entity.AppointmentEntity
import com.agendly.scheduling.infrastructure.persistence.repository.AppointmentJpaRepository
import com.agendly.tenant.infrastructure.persistence.repository.TenantJpaRepository
import com.agendly.tenant.infrastructure.persistence.repository.TenantSettingsJpaRepository
import org.springframework.stereotype.Service
import java.time.*
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class SchedulingService(
  private val tenants: TenantJpaRepository,
  private val tenantSettings: TenantSettingsJpaRepository,
  private val tenantClients: TenantClientJpaRepository,
  private val subscriptions: SubscriptionJpaRepository,
  private val services: ServiceJpaRepository,
  private val planServices: PlanServiceJpaRepository,
  private val staff: StaffMemberJpaRepository,
  private val staffWorkingHours: StaffWorkingHoursJpaRepository,
  private val appointments: AppointmentJpaRepository
) {

  data class TimeSlot(val start: Instant, val end: Instant, val available: Boolean)

  fun availability(tenantSlug: String, serviceId: UUID, staffId: UUID, date: LocalDate): List<TimeSlot> {
    val tenant = tenants.findBySlug(tenantSlug) ?: throw NotFoundException("Tenant not found")
    val staffMember = staff.findByIdAndTenantId(staffId, tenant.id!!) ?: throw NotFoundException("Staff not found")
    val service = services.findById(serviceId).orElseThrow { NotFoundException("Service not found") }
    if (service.tenant.id != tenant.id) throw NotFoundException("Service not found")

    val settings = tenantSettings.findById(tenant.id!!).orElseThrow()
    val zone = ZoneId.of(settings.timezone)
    val dow = date.dayOfWeek.value % 7 // convert Mon=1 -> 1; Sun=7 -> 0
    val blocks = staffWorkingHours.findAllByStaffIdAndDayOfWeek(staffMember.id!!, dow)

    if (blocks.isEmpty()) return emptyList()

    val duration = Duration.ofMinutes(service.durationMin.toLong())
    val from = date.atStartOfDay(zone).toInstant()
    val to = date.plusDays(1).atStartOfDay(zone).toInstant()
    val booked = appointments.findAllByStaffIdAndStartAtBetween(staffMember.id!!, from, to)

    fun overlaps(s: Instant, e: Instant): Boolean =
      booked.any { b -> b.startAt.isBefore(e) && b.endAt.isAfter(s) && b.status == "CONFIRMED" }

    val result = mutableListOf<TimeSlot>()

    for (b in blocks) {
      var cursor = date.atTime(b.startTime).atZone(zone).toInstant()
      val end = date.atTime(b.endTime).atZone(zone).toInstant()
      while (!cursor.plus(duration).isAfter(end)) {
        val slotEnd = cursor.plus(duration)
        val available = !overlaps(cursor, slotEnd)
        result.add(TimeSlot(cursor, slotEnd, available))
        cursor = cursor.plus(duration).truncatedTo(ChronoUnit.MINUTES)
      }
    }

    return result.sortedBy { it.start }
  }

  data class CreateAppointmentCommand(
    val tenantSlug: String,
    val tenantClientId: UUID,
    val staffId: UUID,
    val serviceId: UUID,
    val startAt: Instant,
    val notes: String? = null
  )

  data class AppointmentResult(
    val appointmentId: UUID,
    val status: String,
    val startAt: Instant,
    val endAt: Instant
  )

  fun createAppointment(cmd: CreateAppointmentCommand): AppointmentResult {
    val tenant = tenants.findBySlug(cmd.tenantSlug) ?: throw NotFoundException("Tenant not found")
    val settings = tenantSettings.findById(tenant.id!!).orElseThrow()

    val tenantClient = tenantClients.findById(cmd.tenantClientId).orElseThrow { NotFoundException("Tenant client not found") }
    if (tenantClient.tenant.id != tenant.id) throw NotFoundException("Tenant client not found")

    val service = services.findById(cmd.serviceId).orElseThrow { NotFoundException("Service not found") }
    if (service.tenant.id != tenant.id) throw NotFoundException("Service not found")

    val staffMember = staff.findByIdAndTenantId(cmd.staffId, tenant.id!!) ?: throw NotFoundException("Staff not found")

    val subscription = subscriptions.findByTenantIdAndTenantClientId(tenant.id!!, tenantClient.id!!)
      ?: throw BadRequestException("You must subscribe to a plan to book")

    if (settings.requireActiveSubscriptionToBook && subscription.status != "ACTIVE") {
      throw BadRequestException("Subscription is not active")
    }

    // Plan must allow this service
    val planId = subscription.plan.id!!
    if (!planServices.existsByIdPlanIdAndIdServiceId(planId, service.id!!)) {
      throw BadRequestException("Your plan does not include this service")
    }

    // Credits rule
    subscription.remainingCredits?.let { credits ->
      if (credits <= 0) throw BadRequestException("No credits remaining for this billing cycle")
    }

    // Conflict check
    val endAt = cmd.startAt.plus(Duration.ofMinutes(service.durationMin.toLong()))
    val conflict = appointments.existsByStaffIdAndStartAtLessThanAndEndAtGreaterThan(staffMember.id!!, endAt, cmd.startAt)
    if (conflict) throw BadRequestException("Time slot not available")

    val entity = AppointmentEntity().also {
      it.tenant = tenant
      it.tenantClientId = tenantClient.id!!
      it.staffId = staffMember.id!!
      it.branchId = staffMember.branchId
      it.serviceId = service.id!!
      it.subscriptionId = subscription.id
      it.startAt = cmd.startAt
      it.endAt = endAt
      it.status = "CONFIRMED"
      it.notes = cmd.notes
    }

    val saved = appointments.save(entity)

    subscription.remainingCredits = subscription.remainingCredits?.let { it - 1 }
    subscriptions.save(subscription)

    return AppointmentResult(saved.id!!, saved.status, saved.startAt, saved.endAt)
  }
}
