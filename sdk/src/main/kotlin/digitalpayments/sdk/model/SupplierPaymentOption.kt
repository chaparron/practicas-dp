package digitalpayments.sdk.model

import kotlinx.serialization.Serializable

@Serializable
data class SupplierPaymentOption(
    val supplierId: Long,
    val paymentOptions: Set<PaymentOption>
)
