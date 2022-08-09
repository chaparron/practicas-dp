package adapters.rest.model.errors

import domain.model.errors.DpErrorReason
import kotlinx.serialization.Serializable

@Serializable
data class DigitalPaymentsDetailedError(
    val reason: DpErrorReason,
    val detail: String? = null
)

