package domain.services.providers

class PaymentProviderService(
    private val jpmProviderService: ProviderService
) {
    fun availableProviders(state: String, supplierId: String): List<Provider> {

        val providers: ArrayList<Provider> = ArrayList()

        if (jpmProviderService.isAccepted(state, supplierId)) {
            providers.add(Provider.JP_MORGAN)
        }

        return providers
    }
}
