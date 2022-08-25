package domain.model

import java.math.BigDecimal
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class PaymentExpiration(
    val paymentId: Long,
    @Contextual
    val amount: BigDecimal
)
