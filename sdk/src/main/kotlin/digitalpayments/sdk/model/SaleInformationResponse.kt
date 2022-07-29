package digitalpayments.sdk.model

import kotlinx.serialization.Serializable

@Serializable
data class SaleInformationResponse(
    val bankId: String,
    val merchantId: String,
    val terminalId: String,
    val encData: String
)
