package digitalpayments.sdk.model

import java.math.BigDecimal
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class UpdatePaymentResponse(
    val paymentId: String?,
    val supplierOrderId: Long,
    @Contextual
    val amount: BigDecimal,
    val totalAmount: String,
    val responseCode: String,
    val message: String
)

