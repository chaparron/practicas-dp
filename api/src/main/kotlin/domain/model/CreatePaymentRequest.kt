package domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CreatePaymentRequest(
    val supplierOrderId: String,
    val amount: String
)
