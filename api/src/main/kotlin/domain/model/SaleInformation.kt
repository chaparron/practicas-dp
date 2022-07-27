package domain.model

@kotlinx.serialization.Serializable
data class SaleInformation(
    val bankId: String,
    val merchantId: String,
    val terminalId: String,
    val encData: String
)
