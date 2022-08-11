package domain.services

import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest

interface PaymentExpirationService {
    fun init(paymentId: String): String
}

class DefaultPaymentExpirationService(
    private val sqsClient: SqsClient,
    private val delaySeconds: Int,
    private val queueUrl: String
) : PaymentExpirationService {

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
}

data class AddToExpirationQueueException(val id: String): RuntimeException("Add paymentId $id to expiration queue failed")
