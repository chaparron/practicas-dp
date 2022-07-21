package adapters.rest.validations

import domain.model.errors.JpmcErrorReason
import domain.model.errors.JpmcException
import java.math.BigDecimal

object RequestValidations {

    fun String?.required(reason: JpmcErrorReason): String = if (this.isNullOrBlank()) {
        throw JpmcException.from(reason = reason)
    } else this

    fun Long?.required(reason: JpmcErrorReason): Long = this ?: throw JpmcException.from(reason = reason)

    fun BigDecimal?.required(reason: JpmcErrorReason): BigDecimal = this ?: throw JpmcException.from(reason = reason)

}
