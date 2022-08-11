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

@ExtendWith(MockitoExtension::class)
class PaymentExpirationServiceTest {

    private val sqsClient: SqsClient = mock()

    companion object {
        private const val PAYMENT_EXPIRATION_DELAY_IN_SECONDS = 15
        private const val PAYMENT_EXPIRATION_QUEUE_URL = "some.url"
    }

    private val sut = DefaultPaymentExpirationService(
        sqsClient = sqsClient,
        delaySeconds = PAYMENT_EXPIRATION_DELAY_IN_SECONDS,
        queueUrl = PAYMENT_EXPIRATION_QUEUE_URL
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
}
