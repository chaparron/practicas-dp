package domain.services

import adapters.repositories.supplierorderdelay.DynamoDBOrderDelayRepository
import domain.model.SupplierOrderDelayEvent
import org.slf4j.LoggerFactory

interface SupplierOrderDelayService {
    fun save(supplierOrderDelayEvent: SupplierOrderDelayEvent): SupplierOrderDelayEvent
}

class DefaultSupplierOrderDelayService(
    private val supplierOrderDelayRepository: DynamoDBOrderDelayRepository
) : SupplierOrderDelayService {
    companion object {
        private val logger = LoggerFactory.getLogger(DefaultSupplierOrderDelayService::class.java)
    }

    override fun save(supplierOrderDelayEvent: SupplierOrderDelayEvent): SupplierOrderDelayEvent {
        logger.info("About to save the following supplier order delay: $supplierOrderDelayEvent")
        return supplierOrderDelayRepository.save(supplierOrderDelayEvent)
    }
}
