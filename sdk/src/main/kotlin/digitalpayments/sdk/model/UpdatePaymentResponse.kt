package digitalpayments.sdk.model

import java.math.BigDecimal
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class UpdatePaymentResponse(
    val paymentId: Long,
    val supplierOrderId: Long,
    @Contextual
    val amount: BigDecimal,
    val responseCode: String,
    val message: String
)
