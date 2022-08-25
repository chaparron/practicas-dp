package domain.services

import domain.model.PaymentExpiration
import java.math.BigDecimal
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import wabi2b.payment.async.notification.sdk.WabiPaymentAsyncNotificationSdk
import wabi2b.payments.common.model.dto.PaymentType
import wabi2b.payments.common.model.dto.PaymentUpdated
import wabi2b.payments.common.model.dto.type.PaymentMethod
import wabi2b.payments.common.model.dto.type.PaymentResult

interface PaymentExpirationService {
    fun init(paymentExpiration: PaymentExpiration): PaymentExpiration
    fun expire(paymentExpiration: PaymentExpiration): Boolean
}

class DefaultPaymentExpirationService(
    private val sqsClient: SqsClient,
    private val delaySeconds: Int,
    private val queueUrl: String,
    private val wabiPaymentAsyncNotificationSdk: WabiPaymentAsyncNotificationSdk,
    private val mapper: Json
) : PaymentExpirationService {

    companion object {
        private val logger = LoggerFactory.getLogger(DefaultPaymentExpirationService::class.java)
    }
    override fun init(paymentExpiration: PaymentExpiration): PaymentExpiration {
        SendMessageRequest.builder()
                .messageBody(mapper.encodeToString(paymentExpiration))
            .delaySeconds(delaySeconds)
            .queueUrl(queueUrl)
            .build()
            .let {
                sqsClient.sendMessage(it).sdkHttpResponse().isSuccessful
            }
            .let {
                return if(it) paymentExpiration else throw AddToExpirationQueueException(paymentExpiration.paymentId.toString())
            }
    }

    override fun expire(paymentExpiration: PaymentExpiration): Boolean {
        return paymentExpiration.runCatching {
            logger.info("About to expire paymentId $this")
            wabiPaymentAsyncNotificationSdk.notify(this.toPaymentUpdate())
        }.onSuccess {
            logger.info("PaymentId ${paymentExpiration.paymentId} expired")
        }.onFailure {
            logger.error("There was an error expiring paymentId ${paymentExpiration.paymentId}")
            throw PaymentExpireException(paymentExpiration.paymentId.toString(), it)
        }.map {
            true
        }.getOrThrow()
    }
    private fun PaymentExpiration.toPaymentUpdate() = PaymentUpdated(
        supplierOrderId = null,
        paymentId = paymentId,
        paymentType = PaymentType.DIGITAL_PAYMENT,
        resultType = PaymentResult.EXPIRED,
        amount = amount,
        paymentMethod = null
    )
}

data class AddToExpirationQueueException(val id: String): RuntimeException("Add paymentId $id to expiration queue failed")
data class PaymentExpireException(val id:String, override val cause: Throwable): RuntimeException("There was an error expiring paymentId $id", cause)
