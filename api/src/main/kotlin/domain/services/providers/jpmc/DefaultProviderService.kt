package domain.services.providers.jpmc

import domain.services.SupplierService
import domain.services.providers.ProviderService
import org.slf4j.LoggerFactory

class DefaultProviderService(
    private val supplierService: SupplierService,
) : ProviderService {

    companion object {
        private val logger = LoggerFactory.getLogger(DefaultProviderService::class.java)
    }

    /**
     * This method return if the provider JPMorgan is accepted.
     *
     * Validation: Check associate bank account
     */
    override fun isAccepted(supplierId: String): Boolean {
        return supplierId.runCatching {
            supplierService.get(this).bankAccountNumber.isNotEmpty()
        }.onSuccess {
            logger.info("Supplier retrieved: $it")
        }.onFailure {
            logger.error("There was an error retrieving supplier id: $supplierId")
        }.getOrElse { false }
    }
}
