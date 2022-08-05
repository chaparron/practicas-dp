package digitalpayments.sdk.model

import kotlinx.serialization.Serializable

@Serializable
class CreatePaymentRequest(
    val supplierOrderId: String,
    val amount: String,
    val totalAmount: String
)
