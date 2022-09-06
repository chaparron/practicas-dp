package domain.model.errors

import asJsonString
import domain.model.exceptions.DigitalPaymentsDetailedError
import domain.model.exceptions.ErrorReason

data class MissingFieldException(val field: String, override val cause: Throwable? = null) :
    RuntimeException("Missing required field: $field") {
    fun asMissingFieldException() =
        DigitalPaymentsDetailedError(
            reason = ErrorReason.MISSING_FIELD_EXCEPTION,
            detail = message!!
        ).asJsonString()
}
