package domain.model

import java.math.BigDecimal
import java.time.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class Payment(
    val supplierOrderId: Long,
    val paymentId: Long,
    @Contextual
    val amount: BigDecimal,
    val paymentOption: String? = null,
    val responseCode: String? = null,
    val message: String? = null,
    val encData: String? = null,
    val status: PaymentStatus,
    val createdAt: String,
    val lastUpdatedAt: String
) {
    constructor(
        supplierOrderId: Long,
        paymentId: Long,
        amount: BigDecimal,
        paymentOption: String? = null,
        responseCode: String? = null,
        message: String? = null,
        encData: String? = null,
        status: PaymentStatus,
        created: Instant = Instant.now()
    ): this(
        supplierOrderId, paymentId, amount, paymentOption, responseCode, message, encData, status, created.toString(), created.toString()
    )

    fun updated(at: Instant): Payment {
        return copy(lastUpdatedAt = at.toString())
    }
}

enum class PaymentStatus {
    IN_PROGRESS, PAID, ERROR, EXPIRED
}
