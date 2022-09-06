package domain.functions

import anyPaymentExpiration
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.SQSEvent
import configuration.MainConfiguration
import domain.model.PaymentExpiration
import domain.services.PaymentExpirationService
import domain.services.PaymentExpireException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertFailsWith

@ExtendWith(MockitoExtension::class)
class PaymentExpirationListenerTest {

    private val paymentExpirationService: PaymentExpirationService = mock()
    private val context: Context = mock()
    private val mapper: Json = MainConfiguration.jsonMapper

    private val sut = PaymentExpirationListener(
        paymentExpirationService = paymentExpirationService,
        mapper = mapper
    )

    @Test
    fun `Should handle a valid event`() {
        val somePaymentExpiration = anyPaymentExpiration()
        val sqsEvent = buildSqsEvent(somePaymentExpiration)

        whenever(paymentExpirationService.expire(somePaymentExpiration)).thenReturn(true)

        sut.handleRequest(sqsEvent, context)

        verify(paymentExpirationService).expire(somePaymentExpiration)
    }

    @Test
    fun `Should propagates exception when paymentExpirationService fails`() {
        val somePaymentExpiration = anyPaymentExpiration()
        val sqsEvent = buildSqsEvent(somePaymentExpiration)


        whenever(paymentExpirationService.expire(somePaymentExpiration)).thenThrow(PaymentExpireException(somePaymentExpiration.paymentId.toString(), RuntimeException()))

        assertFailsWith<PaymentExpireException> {
            sut.handleRequest(sqsEvent, context)
        }

        verify(paymentExpirationService).expire(somePaymentExpiration)
    }

    private fun buildSqsEvent(paymentExpiration: PaymentExpiration): SQSEvent = paymentExpiration.let {
        SQSEvent.SQSMessage().apply { body = mapper.encodeToString(paymentExpiration) }.let { SQSEvent().apply { records = listOf(it) } }
    }
}
