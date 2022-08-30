package domain.functions

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.SQSEvent
import configuration.MainConfiguration
import domain.model.PaymentExpiration
import domain.services.PaymentExpirationService
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

class PaymentExpirationListener(
    private val paymentExpirationService: PaymentExpirationService,
    private val mapper:Json
): RequestHandler<SQSEvent, Unit> {

    companion object {
        private val logger = LoggerFactory.getLogger(PaymentExpirationListener::class.java)
    }

    @Suppress("unused")
    constructor() : this(
        paymentExpirationService = MainConfiguration.paymentExpirationService,
        mapper = MainConfiguration.jsonMapper
    )

    override fun handleRequest(input: SQSEvent, context: Context?) {
        logger.info("PaymentId expired event received: $input")
        input.records.forEach { record ->
            record.also {
                logger.info("Received SQS PaymentId expired message: $record")
        }.body.let {
                mapper.decodeFromString<PaymentExpiration>(it)
            }.let {
                paymentExpirationService.expire(it)
            }
        }
    }
}
