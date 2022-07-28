package domain.services.providers.jpmc

import domain.services.state.StateValidatorService
import domain.services.SupplierService
import domain.services.providers.ProviderService
import org.slf4j.LoggerFactory

class JpmProviderService(
    private val supplierService: SupplierService,
    private val stateValidator: StateValidatorService
) : ProviderService {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * This method return if the provider JPMorgan is accepted.
     *
     * Validations:
     *  - State Token validation (customer)
     *  - Check associate bank account and his state (supplier)
     */
    override fun isAccepted(state: String, supplierId: String): Boolean {
        val supplier = supplierService.get(supplierId)
        logger.trace("Supplier state: ${supplier.state}")
        return stateValidator.validate(state) && stateValidator.validate(supplier.state)
    }
}
