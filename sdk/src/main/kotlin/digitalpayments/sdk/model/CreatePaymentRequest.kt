package digitalpayments.sdk.model

import java.math.BigDecimal
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class CreatePaymentRequest(
    val supplierOrderId: Long,
    @Contextual
    val amount: BigDecimal
)
