package domain.model.errors

import asJsonString
import domain.model.exceptions.ErrorReason
import domain.model.exceptions.DigitalPaymentsDetailedError

data class FunctionalityNotAvailable(val state: String, override val cause: Throwable? = null) :
    RuntimeException("The current functionality is not available for $state", cause) {
    fun asFunctionalityNotAvailableError() =
        DigitalPaymentsDetailedError(
            reason = ErrorReason.FUNCTIONALITY_NOT_AVAILABLE,
            detail = message!!
        ).asJsonString()
}
