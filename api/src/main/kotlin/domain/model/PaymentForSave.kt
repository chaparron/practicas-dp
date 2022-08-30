package domain.model

import java.math.BigDecimal
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class PaymentForSave(
    val paymentId: Long,
    val supplierOrderId: Long,
    @Contextual
    val amount: BigDecimal,
    val status: PaymentStatus,
    val invoiceId: String,
    val createdAt: String,
    val lastUpdatedAt: String
)
