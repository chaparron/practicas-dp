package domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BankAccountEvent (
    val number: String,
    @SerialName("ifsc")
    val indianFinancialSystemCode: String
)

@Serializable
data class SupplierEvent(
    @SerialName("id")
    val supplierId: Long,
    val country: String,
    val bankAccount: BankAccountEvent? = null

) {
    fun toSupplier() : Supplier {
        return Supplier(supplierId, bankAccount!!.number, bankAccount.indianFinancialSystemCode)
    }

    fun doHandle(validator: SupplierEventValidator, block: (Supplier) -> Unit) {
        if(validator.isValid(this))
            block(toSupplier())
    }
}

class SupplierEventValidator {

    companion object {
        private val rules = listOf(
            `bank account cannot be null`,
            `country must be supported`
        )
    }

    fun isValid(event: SupplierEvent): Boolean {
        return rules.map {
            it(event)
        }.all { it }
    }
}

typealias SupplierEventRule = (SupplierEvent) -> Boolean

val `bank account cannot be null`: SupplierEventRule = {
    it.bankAccount != null
}

val `country must be supported`: SupplierEventRule = { event ->
    bankAccountSupportedCountries.contains( event.country )
}

val bankAccountSupportedCountries = setOf("in")
