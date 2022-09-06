package domain.model.errors

import asJsonString
import domain.model.exceptions.DigitalPaymentsDetailedError
import domain.model.exceptions.ErrorReason

data class ClientTokenException(val clientUser: String, override val cause: Throwable? = null):
    RuntimeException("An error occur trying to retrieve token for client user $clientUser", cause){

    fun asClientTokenExceptionError() =
        DigitalPaymentsDetailedError(
            reason = ErrorReason.CLIENT_TOKEN_EXCEPTION,
            detail = message!!
        ).asJsonString()

    }
