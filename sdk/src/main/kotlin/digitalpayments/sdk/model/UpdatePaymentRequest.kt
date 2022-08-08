package digitalpayments.sdk.model

import kotlinx.serialization.Serializable

@Serializable
data class UpdatePaymentRequest(
    val encData: String
)
