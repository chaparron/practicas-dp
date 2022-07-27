package domain.model.errors

import domain.model.errors.JpmcErrorReason.FUNCTIONALITY_NOT_AVAILABLE

class FunctionalityNotAvailable(cause: Throwable? = null) : JpmcException(
    reason = FUNCTIONALITY_NOT_AVAILABLE,
    detail = FUNCTIONALITY_NOT_AVAILABLE.detail(),
    rootCause = cause
)
