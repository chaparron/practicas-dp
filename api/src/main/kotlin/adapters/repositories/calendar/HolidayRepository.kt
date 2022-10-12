package adapters.repositories.calendar

import java.time.Instant

interface HolidayRepository {
    fun findWorkingDay(instant: Instant) : Instant
}

class DefaultHolidayRepository: HolidayRepository {
    override fun findWorkingDay(instant: Instant): Instant {
        TODO("Not yet implemented")
    }
}
