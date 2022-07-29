package adapters.rest.model.errors

import domain.model.errors.JpmcErrorReason
import kotlinx.serialization.Serializable

@Serializable
data class DigitalPaymentsDetailedError(
    val reason: JpmcErrorReason,
    val detail: String? = null
)

