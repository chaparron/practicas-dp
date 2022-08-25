package domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UpdatePaymentResponse (
    val paymentId: String?,
    val supplierOrderId: String,
    val amount: String,
    val responseCode: String,
    val message: String
    )

