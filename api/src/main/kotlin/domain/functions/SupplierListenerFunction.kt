package domain.functions

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.SNSEvent
import configuration.MainConfiguration
import domain.model.Supplier
import domain.model.SupplierEvent
import domain.services.SupplierService
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

class SupplierListenerFunction(
    private val jsonMapper: Json,
    val supplierService: SupplierService
) : RequestHandler<SNSEvent, Unit> {

    companion object {
        private val logger = LoggerFactory.getLogger(SupplierListenerFunction::class.java)
    }

    @Suppress("unused")
    constructor() : this(
        supplierService = MainConfiguration.supplierService,
        jsonMapper = MainConfiguration.jsonMapper
    )

    override fun handleRequest(event: SNSEvent, context: Context) {
        event.records.forEach { record ->
            record.apply {
                logger.info("Received SNS message: {}", record)
            }.let {
                deserialize(it.sns.message)
            }.let {
                supplierService.save(it).also { response ->
                    logger.info("Supplier saved {}", response)
                }
            }
        }
    }

    private fun deserialize(message: String): Supplier {
        return jsonMapper.decodeFromString<SupplierEvent>(message).toSupplier()
    }
}
