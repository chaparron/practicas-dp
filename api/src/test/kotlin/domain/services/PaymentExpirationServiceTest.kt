package domain.services


import adapters.repositories.jpmc.JpmcPaymentRepository
import anyPaymentExpiration
import com.wabi2b.jpmc.sdk.usecase.sale.PaymentStatus
import configuration.MainConfiguration
import domain.model.PaymentForStatusUpdate
import java.time.Clock
import java.time.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import software.amazon.awssdk.http.SdkHttpResponse
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import software.amazon.awssdk.services.sqs.model.SendMessageResponse
import wabi2b.payment.async.notification.sdk.WabiPaymentAsyncNotificationSdk
import wabi2b.payments.common.model.dto.PaymentType
import wabi2b.payments.common.model.dto.PaymentUpdated
import wabi2b.payments.common.model.dto.type.PaymentResult
import kotlin.test.assertEquals
import org.mockito.kotlin.verifyNoInteractions

@ExtendWith(MockitoExtension::class)
class PaymentExpirationServiceTest {

    private val sqsClient: SqsClient = mock()
    private val wabiPaymentAsyncNotificationSdk: WabiPaymentAsyncNotificationSdk = mock()
    private val repository: JpmcPaymentRepository = mock()
    private val clock: Clock = mock()
    private val mapper: Json = MainConfiguration.jsonMapper
    companion object {
        private const val PAYMENT_EXPIRATION_DELAY_IN_SECONDS = 15
        private const val PAYMENT_EXPIRATION_QUEUE_URL = "some.url"
    }

    private val sut = DefaultPaymentExpirationService(
        sqsClient = sqsClient,
        delaySeconds = PAYMENT_EXPIRATION_DELAY_IN_SECONDS,
        queueUrl = PAYMENT_EXPIRATION_QUEUE_URL,
        wabiPaymentAsyncNotificationSdk = wabiPaymentAsyncNotificationSdk,
        jpmcPaymentRepository = repository,
        clock = clock,
        mapper = mapper
    )

    @Test
    fun `Push paymentId to expiration queue`() {
        val somePaymentExpiration = anyPaymentExpiration()
        val request = SendMessageRequest.builder()
            .messageBody(mapper.encodeToString(somePaymentExpiration))
            .delaySeconds(PAYMENT_EXPIRATION_DELAY_IN_SECONDS)
            .queueUrl(PAYMENT_EXPIRATION_QUEUE_URL)
            .build()

        val sdkHttpResponse = SdkHttpResponse.builder().statusCode(200).build()
        val sendMessageResponse = SendMessageResponse.builder().apply {
            sdkHttpResponse(sdkHttpResponse)
        }.build()

        whenever(sqsClient.sendMessage(request)).thenReturn(sendMessageResponse)

        assertEquals(somePaymentExpiration, sut.init(somePaymentExpiration))
        verify(sqsClient, times(1)).sendMessage(request)
    }

    @Test
    fun `Throw AddToExpirationQueueException when sendMessage is not successful`() {
        val somePaymentExpiration = anyPaymentExpiration()
        val request = SendMessageRequest.builder()
            .messageBody(mapper.encodeToString(somePaymentExpiration))
            .delaySeconds(PAYMENT_EXPIRATION_DELAY_IN_SECONDS)
            .queueUrl(PAYMENT_EXPIRATION_QUEUE_URL)
            .build()

        val sdkHttpResponse = SdkHttpResponse.builder().statusCode(400).build()
        val sendMessageResponse = SendMessageResponse.builder().apply {
            sdkHttpResponse(sdkHttpResponse)
        }.build()

        whenever(sqsClient.sendMessage(request)).thenReturn(sendMessageResponse)

        assertThrows<AddToExpirationQueueException> {
            sut.init(somePaymentExpiration)
        }
    }

    @Test
    fun `Should expire a paymentId`() {
        val somePaymentExpiration = anyPaymentExpiration()
        val paymentUpdate = PaymentUpdated(
            supplierOrderId = somePaymentExpiration.supplierOrderId,
            paymentId = somePaymentExpiration.paymentId,
            paymentType = PaymentType.DIGITAL_PAYMENT,
            resultType = PaymentResult.EXPIRED,
            amount = somePaymentExpiration.amount,
            paymentMethod = null
        )
        val now = Instant.now()
        val paymentForStatusUpdate = PaymentForStatusUpdate(
            paymentUpdate.paymentId,
            PaymentStatus.EXPIRED,
            now.toString()
        )

        whenever(clock.instant()).thenReturn(now)

        val result = sut.expire(somePaymentExpiration)

        assertEquals(true, result)
        verify(wabiPaymentAsyncNotificationSdk).notify(paymentUpdate)
        verify(repository).update(paymentForStatusUpdate)
    }

    @Test
    fun `Throw PaymentExpireException when expire fails`() {
        val somePaymentExpiration = anyPaymentExpiration()
        val paymentUpdate = PaymentUpdated(
            supplierOrderId = somePaymentExpiration.supplierOrderId,
            paymentId = somePaymentExpiration.paymentId,
            paymentType = PaymentType.DIGITAL_PAYMENT,
            resultType = PaymentResult.EXPIRED,
            amount = somePaymentExpiration.amount,
            paymentMethod = null
        )

        whenever(wabiPaymentAsyncNotificationSdk.notify(paymentUpdate)).thenThrow(java.lang.RuntimeException())

        assertThrows<PaymentExpireException> {
            sut.expire(somePaymentExpiration)
        }
        verify(wabiPaymentAsyncNotificationSdk).notify(paymentUpdate)
        verifyNoInteractions(repository)
    }
}
