package domain.services

import adapters.repositories.calendar.DefaultHolidayRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*

@ExtendWith(MockitoExtension::class)
internal class ReportDateServiceTest {

    @Mock
    private lateinit var holidayRepository: DefaultHolidayRepository

    @InjectMocks
    private lateinit var sut: ReportDateService

    @Test
    fun `reportDate should return a Date`() {
        // Given
        val instant = Instant.parse("2022-10-03T06:47:00Z")
        whenever(holidayRepository.findWorkingDay(instant)).thenReturn(instant)
        // When
        val workday = sut.reportDate(instant)
        // Then
        println("\u001b[7m$workday\u001b[0m")
        verify(holidayRepository).findWorkingDay(instant)
    }
    @Test
    fun `should return the the same date from instant at 10hrs in working day`() {
        // Given
        val instant = Instant.parse("2022-10-03T06:47:00Z")
        whenever(holidayRepository.findWorkingDay(instant)).thenReturn(instant)
        // When
        val workDay = sut.reportDate(instant)
        // Then
        val expected= Date.from(instant)
        assertEquals(expected, workDay)
        verify(holidayRepository).findWorkingDay(instant)
    }
    @Test
    fun `should return the the next date from instant at 19hrs in working day`() {
        // Given
        val instant = Instant.parse("2022-10-03T19:47:00Z")
        val expectedInstant = Instant.parse("2022-10-04T19:47:00Z")
        whenever(holidayRepository.findWorkingDay(expectedInstant)).thenReturn(expectedInstant)
        // When
        val workDay = sut.reportDate(instant)
        // Then
        val expected= Date.from(expectedInstant)
        assertEquals(expected, workDay)
        verify(holidayRepository).findWorkingDay(expectedInstant)
    }
}
