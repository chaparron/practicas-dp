package digitalpayments.sdk.model

import kotlinx.serialization.Serializable

@Serializable
class UpdatePaymentResponse (
    val paymentId: String?,
    val supplierOrderId: String,
    val amount: String,
    val totalAmount: String,
    val responseCode: String,
    val message: String
    )

