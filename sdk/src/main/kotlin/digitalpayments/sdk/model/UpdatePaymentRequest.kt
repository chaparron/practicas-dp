package digitalpayments.sdk.model

import kotlinx.serialization.Serializable

@Serializable
class UpdatePaymentRequest(
    val encData: String
)
