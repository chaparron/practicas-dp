package domain.model.errors

import asJsonString
import domain.model.PaymentForUpdate
import domain.model.exceptions.DigitalPaymentsDetailedError
import domain.model.exceptions.ErrorReason

data class UpdatePaymentException(val payment: PaymentForUpdate, override val cause: Throwable? = null)
    : RuntimeException("There was an error updating the following payment: $payment"){
    fun asUpdatePaymentException() =
        DigitalPaymentsDetailedError(
            reason = ErrorReason.UPDATE_PAYMENT_EXCEPTION,
            detail = message!!
        ).asJsonString()
    }


