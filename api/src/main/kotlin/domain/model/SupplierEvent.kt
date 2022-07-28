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
    val supplierId: String,
    val state: String,
    @SerialName("bankAccountDataEvent")
    val bankAccount: BankAccountEvent

) {
    fun toSupplier() : Supplier {
        return Supplier(supplierId, state, bankAccount.number, bankAccount.indianFinancialSystemCode)
    }
}
