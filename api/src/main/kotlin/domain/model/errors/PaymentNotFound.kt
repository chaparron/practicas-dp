package domain.model.errors

import asJsonString
import domain.model.exceptions.DigitalPaymentsDetailedError
import domain.model.exceptions.ErrorReason

data class PaymentNotFound(val jpmcId: String, override val cause: Throwable? = null)
    : RuntimeException("Cannot find any jpmc information for $jpmcId", cause){
    fun asPaymentNotFoundError() =
        DigitalPaymentsDetailedError(
            reason = ErrorReason.PAYMENT_NOT_FOUND_EXCEPTION,
            detail = message!!
        ).asJsonString()
    }
