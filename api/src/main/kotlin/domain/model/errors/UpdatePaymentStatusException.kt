package domain.model.errors

import asJsonString
import domain.model.PaymentForStatusUpdate
import domain.model.exceptions.DigitalPaymentsDetailedError
import domain.model.exceptions.ErrorReason

data class UpdatePaymentStatusException(val payment: PaymentForStatusUpdate, override val cause: Throwable? = null)
    : RuntimeException("There was an error updating the following payment: $payment"){
    fun asUpdatePaymentStatusException() =
        DigitalPaymentsDetailedError(
            reason = ErrorReason.UPDATE_PAYMENT_EXCEPTION,
            detail = message!!
        ).asJsonString()
    }
