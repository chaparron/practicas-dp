package domain.services.providers

interface ProviderService {

    fun isAccepted(state: String, supplierId: String): Boolean

}
