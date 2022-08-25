package domain.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import randomString
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals
import randomBigDecimal
import randomLong

class PaymentTest {

    @Test
    fun `when createdAt is assigned lastUpdatedAt must be assigned as well`() {
        //given
        val now = now()
        val nowAsString = now.toString()

        //when
        val payment = anyPayment(now)

        //then
        assertAll(
            "will verify dates",
            { assertEquals(nowAsString, payment.createdAt) },
            { assertEquals(nowAsString, payment.lastUpdatedAt) }
        )
    }

    @Test
    fun `when lastUpdatedAt is assigned createdAt remains the same`() {
        //given
        val now = now()
        val nowAsString = now.toString()
        val tomorrow = now.plus(1, ChronoUnit.DAYS)
        val tomorrowAsString = tomorrow.toString()
        val payment = anyPayment(now)

        //when
        val updated = payment.updated(tomorrow)

        //then
        assertAll(
            "will verify dates",
            { assertEquals(nowAsString, payment.createdAt) },
            { assertEquals(tomorrowAsString, updated.lastUpdatedAt) }
        )
    }

    private fun now(): Instant = Instant.now()

    private fun anyPayment(created: Instant = now()): Payment = Payment(
        supplierOrderId = randomLong(),
        paymentId = randomLong(),
        amount = randomBigDecimal(),
        status = PaymentStatus.EXPIRED,
        created = created
    )
}
