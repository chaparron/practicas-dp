package adapters.rest.model.errors

import kotlinx.serialization.Serializable

@Serializable
data class DigitalPaymentsError(
    val type: String? = null,
    val entity: String,
    val property: String,
    val invalidValue: String,
    val message: String
)

