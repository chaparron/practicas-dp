package domain.services.providers

interface ProviderService {
    fun isAccepted(supplierId: Long): Boolean
}
