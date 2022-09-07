package adapters.rest.handler

import asJsonString
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import domain.model.errors.*
import domain.model.exceptions.DigitalPaymentsDetailedError
import domain.model.exceptions.ErrorReason
import kotlinx.serialization.json.Json
import org.apache.http.HttpStatus
import org.slf4j.LoggerFactory
import wabi.sdk.impl.CustomSdkException

open class ErrorHandler(val json: Json) : wabi.rest2lambda.ErrorHandler {

    companion object {
        private val log = LoggerFactory.getLogger(ErrorHandler::class.java)
    }

    override fun handle(error: Throwable): APIGatewayProxyResponseEvent {
        log.trace("Exception caught: ${error.message}", error)
        return when (error) {
            is FunctionalityNotAvailable -> createRequest(
                error.asFunctionalityNotAvailableError(),
                HttpStatus.SC_BAD_REQUEST
            )
            is ClientTokenException -> createRequest(error.asClientTokenExceptionError(), HttpStatus.SC_BAD_REQUEST)
            is MissingFieldException -> createRequest(error.asMissingFieldException(), HttpStatus.SC_BAD_REQUEST)
            is UpdatePaymentException -> createRequest(error.asUpdatePaymentException(), HttpStatus.SC_BAD_REQUEST)
            is UpdatePaymentStatusException -> createRequest(error.asUpdatePaymentStatusException(), HttpStatus.SC_BAD_REQUEST)
            is PaymentNotFound -> createRequest(error.asPaymentNotFoundError(), HttpStatus.SC_BAD_REQUEST)
            is CustomSdkException -> createRequest(error.toDetailedError().asJsonString(), HttpStatus.SC_BAD_REQUEST)
            else -> createRequest(defaultDetailedError(error).asJsonString(), HttpStatus.SC_INTERNAL_SERVER_ERROR)

        }.also {
            log.trace("Response: $it")
        }
    }

    private fun createRequest(body: String, status: Int) =
        APIGatewayProxyResponseEvent()
            .withStatusCode(status)
            .withHeaders(
                mapOf(
                    "Content-Type" to "application/json",
                    "X_Custom_Error" to "true"
                )
            )
            .withBody(body)!!

    private fun CustomSdkException.toDetailedError() =
        DigitalPaymentsDetailedError(
            reason = ErrorReason.find(error.reason),
            detail = error.detail!!
        )

    private fun defaultDetailedError(error: Throwable) = DigitalPaymentsDetailedError(
        reason = ErrorReason.UNKNOWN,
        detail = error.message ?: ErrorReason.UNKNOWN.detail()
    )
}
