package domain.services.providers

class PaymentProviderService(
    private val jpmProviderService: ProviderService
) {
    companion object {
        private val defaultProvider = Provider.JP_MORGAN
    }
    fun availableProviders(supplierId: Long): List<Provider> {
        return buildList {
            if(jpmProviderService.isAccepted(supplierId)) add(defaultProvider)
        }
    }
}
