package domain.model

@kotlinx.serialization.Serializable
data class CreatePaymentResponse(
    val bankId: String,
    val merchantId: String,
    val terminalId: String,
    val encData: String
)
