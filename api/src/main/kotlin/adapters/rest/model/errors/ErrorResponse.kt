package adapters.rest.model.errors

import kotlinx.serialization.Serializable


@Serializable
data class ErrorResponse(val errors: List<DigitalPaymentsError>)
