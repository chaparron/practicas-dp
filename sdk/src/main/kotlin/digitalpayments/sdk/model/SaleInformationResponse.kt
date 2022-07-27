package digitalpayments.sdk.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SaleInformationResponse(
    @SerialName(value = "BankId")
    val bankId: String,
    @SerialName(value = "MerchantId")
    val merchantId: String,
    @SerialName(value = "TerminalId")
    val terminalId: String,
    @SerialName(value = "EncData")
    val encData: String
)
