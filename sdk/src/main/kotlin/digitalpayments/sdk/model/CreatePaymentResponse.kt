package digitalpayments.sdk.model

import kotlinx.serialization.Serializable

@Serializable
data class CreatePaymentResponse(
    val bankId: String,
    val merchantId: String,
    val terminalId: String,
    val encData: String
)
