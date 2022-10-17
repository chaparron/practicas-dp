package domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SupplierOrderDelayEvent(
    val supplierOrderId: String,
    val delay: Boolean,
    val delayTime: Int,
) { // payload (payload = json) {"supplierOrderId":"2334","delay":true,"delayTime":"2 hours"}

    fun toSupplierOrderDelayEvent(): SupplierOrderDelayEvent{
        return SupplierOrderDelayEvent(supplierOrderId, delay, delayTime)
    }
    fun doHandle(validator: SupplierOrderDelayValidator, block: (SupplierOrderDelayEvent) -> Unit) {
        if(validator.isValid(this))
            block(toSupplierOrderDelayEvent())
    }
}

class SupplierOrderDelayValidator {

    companion object {
        private val rules = listOf(
//            `Supplier order id cannot be null`,
            `Delay time cannot be negative`
        )
    }

    fun isValid(event: SupplierOrderDelayEvent): Boolean {
        return rules.map {
            it(event)
        }.all { it }
    }

}
typealias OrderDelayRule = (SupplierOrderDelayEvent) -> Boolean

//val `Supplier order id cannot be null`: OrderDelayRule = {
//    it.supplierOrderId != null
//}

val `Delay time cannot be negative`: OrderDelayRule = {
    it.delayTime >= 0
}
