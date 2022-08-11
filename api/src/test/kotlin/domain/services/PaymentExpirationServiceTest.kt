package domain.services


import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import randomString
import software.amazon.awssdk.http.SdkHttpResponse
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import software.amazon.awssdk.services.sqs.model.SendMessageResponse
import wabi2b.payment.async.notification.sdk.WabiPaymentAsyncNotificationSdk
import wabi2b.payments.common.model.dto.PaymentType
import wabi2b.payments.common.model.dto.PaymentUpdated
import wabi2b.payments.common.model.dto.type.PaymentResult

@ExtendWith(MockitoExtension::class)
class PaymentExpirationServiceTest {

    private val sqsClient: SqsClient = mock()
    private val wabiPaymentAsyncNotificationSdk: WabiPaymentAsyncNotificationSdk = mock()
    companion object {
        private const val PAYMENT_EXPIRATION_DELAY_IN_SECONDS = 15
        private const val PAYMENT_EXPIRATION_QUEUE_URL = "some.url"
    }

    private val sut = DefaultPaymentExpirationService(
        sqsClient = sqsClient,
        delaySeconds = PAYMENT_EXPIRATION_DELAY_IN_SECONDS,
        queueUrl = PAYMENT_EXPIRATION_QUEUE_URL,
        wabiPaymentAsyncNotificationSdk = wabiPaymentAsyncNotificationSdk
    )

    @Test
    fun `Push paymentId to expiration queue`() {
        val somePaymentId = randomString()
        val request = SendMessageRequest.builder()
            .messageBody(somePaymentId)
            .delaySeconds(PAYMENT_EXPIRATION_DELAY_IN_SECONDS)
            .queueUrl(PAYMENT_EXPIRATION_QUEUE_URL)
            .build()

        val sdkHttpResponse = SdkHttpResponse.builder().statusCode(200).build()
        val sendMessageResponse = SendMessageResponse.builder().apply {
            sdkHttpResponse(sdkHttpResponse)
        }.build()

        whenever(sqsClient.sendMessage(request)).thenReturn(sendMessageResponse)

        assertEquals(somePaymentId, sut.init(somePaymentId))
        verify(sqsClient, times(1)).sendMessage(request)
    }

    @Test
    fun `Throw AddToExpirationQueueException when sendMessage is not successful`() {
        val somePaymentId = randomString()
        val request = SendMessageRequest.builder()
            .messageBody(somePaymentId)
            .delaySeconds(PAYMENT_EXPIRATION_DELAY_IN_SECONDS)
            .queueUrl(PAYMENT_EXPIRATION_QUEUE_URL)
            .build()

        val sdkHttpResponse = SdkHttpResponse.builder().statusCode(400).build()
        val sendMessageResponse = SendMessageResponse.builder().apply {
            sdkHttpResponse(sdkHttpResponse)
        }.build()

        whenever(sqsClient.sendMessage(request)).thenReturn(sendMessageResponse)

        assertThrows<AddToExpirationQueueException> {
            sut.init(somePaymentId)
        }
    }

    @Test
    fun `Should expire a paymentId`() {
        val somePaymentId = 400L
        val paymentUpdate = PaymentUpdated(
            supplierOrderId = null,
            paymentId = somePaymentId,
            paymentType = PaymentType.DIGITAL_PAYMENT,
            resultType = PaymentResult.EXPIRED
        )

        val result = sut.expire(somePaymentId.toString())

        assertEquals(true, result)
        verify(wabiPaymentAsyncNotificationSdk, times(1)).notify(paymentUpdate)
    }

    @Test
    fun `Throw PaymentExpireException when expire fails`() {
        val somePaymentId = 400L
        val paymentUpdate = PaymentUpdated(
            supplierOrderId = null,
            paymentId = somePaymentId,
            paymentType = PaymentType.DIGITAL_PAYMENT,
            resultType = PaymentResult.EXPIRED
        )

        whenever(wabiPaymentAsyncNotificationSdk.notify(paymentUpdate)).thenThrow(java.lang.RuntimeException())

        assertThrows<PaymentExpireException> {
            sut.expire(somePaymentId.toString())
        }
        verify(wabiPaymentAsyncNotificationSdk, times(1)).notify(paymentUpdate)
    }
}
