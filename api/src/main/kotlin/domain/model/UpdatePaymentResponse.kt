package domain.model

import kotlinx.serialization.Serializable

@Serializable
class UpdatePaymentResponse (
    val paymentId: String?,
    val supplierOrderId: String,
    val amount: String,
    val responseCode: String,
    val message: String
    )

