package domain.services

import adapters.repositories.supplier.SupplierRepository
import domain.model.Supplier
import org.slf4j.LoggerFactory

interface SupplierService {
    fun save(supplier: Supplier): Supplier
    fun get(supplierId: String): Supplier
}

class DefaultSupplierService(
    private val supplierRepository: SupplierRepository
) : SupplierService {

    companion object {
        private val logger = LoggerFactory.getLogger(DefaultSupplierService::class.java)
    }

    override fun save(supplier: Supplier): Supplier {
        logger.info("About to save the following supplier $supplier")
        return supplierRepository.save(supplier)
    }

    override fun get(supplierId: String): Supplier {
        logger.info("About to get supplier for $supplierId")
        return supplierRepository.get(supplierId)
    }
}
