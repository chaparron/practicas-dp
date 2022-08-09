package domain.model.errors

import domain.model.errors.DpErrorReason.FUNCTIONALITY_NOT_AVAILABLE

class FunctionalityNotAvailable(cause: Throwable? = null) : DpException(
    reason = FUNCTIONALITY_NOT_AVAILABLE,
    detail = FUNCTIONALITY_NOT_AVAILABLE.detail(),
    rootCause = cause
)
