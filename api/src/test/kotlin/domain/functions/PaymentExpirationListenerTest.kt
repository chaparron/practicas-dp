package domain.functions

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.SQSEvent
import domain.services.PaymentExpirationService
import domain.services.PaymentExpireException
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.times
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import randomString

@ExtendWith(MockitoExtension::class)
class PaymentExpirationListenerTest {

    private val paymentExpirationService: PaymentExpirationService = mock()
    private val context: Context = mock()

    private val sut = PaymentExpirationListener(
        paymentExpirationService = paymentExpirationService
    )

    @Test
    fun `Should handle a valid event`() {
        val anyPaymentId = randomString()
        val sqsEvent = buildSqsEvent(anyPaymentId)

        whenever(paymentExpirationService.expire(anyPaymentId)).thenReturn(true)

        sut.handleRequest(sqsEvent, context)

        verify(paymentExpirationService, times (1)).expire(anyPaymentId)
    }

    @Test
    fun `Should propagates exception when paymentExpirationService fails`() {
        val anyPaymentId = randomString()
        val sqsEvent = buildSqsEvent(anyPaymentId)

        whenever(paymentExpirationService.expire(anyPaymentId)).thenThrow(PaymentExpireException(anyPaymentId, RuntimeException()))

        assertFailsWith<PaymentExpireException> {
            sut.handleRequest(sqsEvent, context)
        }

        verify(paymentExpirationService, times (1)).expire(anyPaymentId)
    }

    private fun buildSqsEvent(paymentId: String): SQSEvent = paymentId.let {
        SQSEvent.SQSMessage().apply { body = it }.let { SQSEvent().apply { records = listOf(it) } }
    }
}
