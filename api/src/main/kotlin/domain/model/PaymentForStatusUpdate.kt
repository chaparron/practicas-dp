package domain.model

import com.wabi2b.jpmc.sdk.usecase.sale.PaymentStatus
import kotlinx.serialization.Serializable

@Serializable
data class PaymentForStatusUpdate(
    val paymentId: Long,
    val status: PaymentStatus,
    val lastUpdatedAt: String
    )
