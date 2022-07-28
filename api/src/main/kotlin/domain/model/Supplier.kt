package domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Supplier(
    val supplierId: String,
    val state: String,
    val bankAccountNumber: String,
    val indianFinancialSystemCode: String
)
