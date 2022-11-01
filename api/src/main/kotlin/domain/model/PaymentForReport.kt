package domain.model

import com.wabi2b.jpmc.sdk.usecase.sale.PaymentStatus
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import wabi2b.payments.common.model.dto.PaymentType
import wabi2b.payments.common.model.dto.type.PaymentMethod
import java.math.BigDecimal
import java.util.*

@Serializable
data class PaymentForReport(
    @Contextual
    val createdAt: String,
    @Contextual
    val reportDay: String,
    val paymentId: Long,
    val supplierOrderId: Long,
    @Contextual
    val amount: BigDecimal,
    val paymentOption: String,
    val encData: String,
    val paymentType: PaymentType,
    val paymentMethod: PaymentMethod,
)
