package domain.model

import kotlinx.serialization.Serializable

@Serializable
data class BankAccount(
    val supplierId: String,
    val number: String,
    val indianFinancialSystemCode: String
)
