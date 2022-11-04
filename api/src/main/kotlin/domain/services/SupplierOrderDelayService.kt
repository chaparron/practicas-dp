package domain.services

import adapters.repositories.supplierorderdelay.SupplierOrderDelayRepository
import domain.model.SupplierOrderDelay
import domain.model.SupplierOrderDelayEvent
import org.slf4j.LoggerFactory

interface SupplierOrderDelayService {
    fun save(supplierOrderDelayEvent: SupplierOrderDelayEvent): SupplierOrderDelay
    fun get(supplierId: Long): SupplierOrderDelay
    fun isDelayed(supplierId: Long): Boolean
}

class DefaultSupplierOrderDelayService(
    private val supplierOrderDelayRepository: SupplierOrderDelayRepository
) : SupplierOrderDelayService {
    companion object {
        private val logger = LoggerFactory.getLogger(DefaultSupplierOrderDelayService::class.java)
    }

    override fun save(supplierOrderDelayEvent: SupplierOrderDelayEvent): SupplierOrderDelay {
        logger.info("About to save the following supplier order delay: $supplierOrderDelayEvent")
        return supplierOrderDelayRepository.save(supplierOrderDelayEvent.toSupplierOrderDelay())
    }

    override fun get(supplierId: Long): SupplierOrderDelay {
        logger.info("About to get supplier order delay: $supplierId")
        return supplierOrderDelayRepository.get(supplierId)
    }

    override fun isDelayed(supplierId: Long) = get(supplierId).delay

    private fun SupplierOrderDelayEvent.toSupplierOrderDelay(): SupplierOrderDelay {
        return SupplierOrderDelay(
            supplierOrderId = this.supplierOrderId,
            delay = this.delay,
            delayTime = this.delayTime
        )
    }
}
