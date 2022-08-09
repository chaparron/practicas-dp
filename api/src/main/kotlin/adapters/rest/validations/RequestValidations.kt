package adapters.rest.validations

import domain.model.errors.DpErrorReason
import domain.model.errors.DpException
import java.math.BigDecimal

object RequestValidations {

    fun String?.required(reason: DpErrorReason): String = if (this.isNullOrBlank()) {
        throw DpException.from(reason = reason)
    } else this

    fun Long?.required(reason: DpErrorReason): Long = this ?: throw DpException.from(reason = reason)

    fun BigDecimal?.required(reason: DpErrorReason): BigDecimal = this ?: throw DpException.from(reason = reason)

}
