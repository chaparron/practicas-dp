package domain.services

import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import wabi2b.payment.async.notification.sdk.WabiPaymentAsyncNotificationSdk
import wabi2b.payments.common.model.dto.PaymentType
import wabi2b.payments.common.model.dto.PaymentUpdated
import wabi2b.payments.common.model.dto.type.PaymentResult

interface PaymentExpirationService {
    fun init(paymentId: String): String
    fun expire(paymentId: String): Boolean
}

class DefaultPaymentExpirationService(
    private val sqsClient: SqsClient,
    private val delaySeconds: Int,
    private val queueUrl: String,
    private val wabiPaymentAsyncNotificationSdk: WabiPaymentAsyncNotificationSdk
) : PaymentExpirationService {

    companion object {
        private val logger = LoggerFactory.getLogger(DefaultPaymentExpirationService::class.java)
    }
    override fun init(paymentId: String): String {
        SendMessageRequest.builder()
            .messageBody(paymentId)
            .delaySeconds(delaySeconds)
            .queueUrl(queueUrl)
            .build()
            .let {
                sqsClient.sendMessage(it).sdkHttpResponse().isSuccessful
            }
            .let {
                return if(it) paymentId else throw AddToExpirationQueueException(paymentId)
            }
    }

    override fun expire(paymentId: String): Boolean {
        return paymentId.runCatching {
            logger.info("About to expire paymentId $this")
            wabiPaymentAsyncNotificationSdk.notify(this.toPaymentUpdate())
        }.onSuccess {
            logger.info("PaymentId $paymentId expired")
        }.onFailure {
            logger.error("There was an error expiring paymentId $paymentId")
            throw PaymentExpireException(paymentId, it)
        }.map {
            true
        }.getOrThrow()
    }
    private fun String.toPaymentUpdate() = PaymentUpdated(
        supplierOrderId = null,
        paymentId = this.toLong(),
        paymentType = PaymentType.DIGITAL_PAYMENT,
        resultType = PaymentResult.EXPIRED
    )
}

data class AddToExpirationQueueException(val id: String): RuntimeException("Add paymentId $id to expiration queue failed")
data class PaymentExpireException(val id:String, override val cause: Throwable): RuntimeException("There was an error expiring paymentId $id", cause)
