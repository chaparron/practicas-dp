package domain.services.providers

class PaymentProviderService(
    private val jpmProviderService: ProviderService
) {
    companion object {
        private val defaultProvider = Provider.JP_MORGAN
    }
    fun availableProviders(state: String, supplierId: String): List<Provider> {
        return buildList {
            if(jpmProviderService.isAccepted(state, supplierId)) add(defaultProvider)
        }
    }
}
