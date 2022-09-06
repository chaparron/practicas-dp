package domain.model.exceptions

import kotlinx.serialization.Serializable

@Serializable
data class DigitalPaymentsDetailedError(
    val reason: ErrorReason,
    val detail: String
)
