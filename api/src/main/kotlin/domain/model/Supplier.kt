package domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Supplier(
    val supplierId: Long,
    val bankAccountNumber: String,
    val indianFinancialSystemCode: String
)
