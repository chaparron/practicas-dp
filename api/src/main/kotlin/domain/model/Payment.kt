package domain.model

import java.time.Instant

data class Payment(
    val supplierOrderId: String,
    val paymentId: String,
    val amount: String,
    val paymentOption: String? = null,
    val responseCode: String? = null,
    val message: String? = null,
    val encData: String? = null,
    val status: PaymentStatus,
    val createdAt: String,
    val lastUpdatedAt: String
) {
    constructor(
        supplierOrderId: String,
        paymentId: String,
        amount: String,
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
