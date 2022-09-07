package domain.model

import com.wabi2b.jpmc.sdk.usecase.sale.PaymentStatus
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.Instant

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
    val lastUpdatedAt: String,
    val invoiceId: String? = null) {
    constructor(
        supplierOrderId: Long,
        paymentId: Long,
        amount: BigDecimal,
        paymentOption: String? = null,
        responseCode: String? = null,
        message: String? = null,
        encData: String? = null,
        status: PaymentStatus,
        created: Instant = Instant.now(),
        invoiceId: String? = null
    ): this(
        supplierOrderId, paymentId, amount, paymentOption, responseCode, message, encData, status, created.toString(), created.toString(), invoiceId
    )

    fun updated(at: Instant): Payment {
        return copy(lastUpdatedAt = at.toString())
    }
}
