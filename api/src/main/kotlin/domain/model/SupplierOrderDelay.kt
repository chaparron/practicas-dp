package domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SupplierOrderDelay(
    val supplierOrderId: Long,
    val delay: Boolean,
    val delayTime: Int,
)
