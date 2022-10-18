package digitalpayments.sdk.model

data class DelayedOrderSupplier(
    val supplierOrderId: Long,
    val delay: Boolean,
    val delayTime: Int,
)
