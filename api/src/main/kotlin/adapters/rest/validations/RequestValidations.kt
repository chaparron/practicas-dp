package adapters.rest.validations

import domain.model.errors.MissingFieldException

object RequestValidations {

    fun String?.required(fieldName: String): String = if (this.isNullOrBlank()) {
        throw MissingFieldException(fieldName)
    } else this

}
