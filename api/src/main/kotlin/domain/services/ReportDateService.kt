package domain.services

import adapters.repositories.calendar.DefaultHolidayRepository
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*

class ReportDateService(
    private val holidayRepository: DefaultHolidayRepository
) {
    fun reportDate(instant: Instant): Date = instant.getWorkingDay().formatDate()

    private fun Instant.getWorkingDay(): Instant {
        val hour = LocalDateTime.ofInstant(this, ZoneOffset.UTC).hour
        return if (hour < 18)
            holidayRepository.findWorkingDay(this)
        else holidayRepository.findWorkingDay(this.plusDay())

    }

    private fun Instant.plusDay(): Instant = this.plus(1, ChronoUnit.DAYS)

    private fun Instant.formatDate() = Date.from(this)

}
