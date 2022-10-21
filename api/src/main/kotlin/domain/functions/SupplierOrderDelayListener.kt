package domain.functions

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.SNSEvent
import configuration.MainConfiguration
import domain.model.SupplierOrderDelayEvent
import domain.model.SupplierOrderDelayValidator
import domain.services.SupplierOrderDelayService
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

class SupplierOrderDelayListener(
    private val jsonMapper: Json = MainConfiguration.jsonMapper,
    private val supplierOrderDelayService: SupplierOrderDelayService = MainConfiguration.supplierOrderDelayService
) : RequestHandler<SNSEvent, Unit> {

    companion object {
        private val logger = LoggerFactory.getLogger(SupplierOrderDelayListener::class.java)
        private val validator = SupplierOrderDelayValidator()
    }

    override fun handleRequest(event: SNSEvent, context: Context) {
        logger.info("Event: $event")
        event.records.forEach { record ->
            record.apply {
                logger.info("Received SNS message: {}", record)
            }.let {
                deserialize(it.sns.message)
            }.let {
                it.doHandle(validator) { event ->
                    supplierOrderDelayService.save(event).also { response ->
                        logger.info("Supplier order delay event saved {}", response)
                    }
                }
            }
        }
    }

    private fun deserialize(message: String): SupplierOrderDelayEvent {
        return jsonMapper.decodeFromString(message)
    }


}

