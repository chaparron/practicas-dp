package adapters.rest.handler

import adapters.rest.model.errors.DigitalPaymentsDetailedError
import adapters.rest.validations.Security
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import domain.model.errors.JpmcException
import domain.model.errors.ErrorType.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import wabi.rest2lambda.*

class RestErrorHandler(private val jsonMapper: Json) : ErrorHandler {

    companion object {
        private val defaultHandler = DefaultErrorHandler()
    }

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun handle(error: Throwable): APIGatewayProxyResponseEvent {
        logger.error("Attempt to handler error.", error)
        return when (error) {
            is JpmcException -> {
                val detailedError = DigitalPaymentsDetailedError(reason = error.reason, detail = error.detail)
                when (error.reason.type) {
                    BAD_REQUEST -> customError(detailedError, 400)
                    NOT_FOUND -> customError(detailedError, 404)
                    CONFLICT -> customError(detailedError, 409)
                    else -> customError(detailedError, 500)
                }
            }
            is Security.ForbiddenException -> forbidden(error.message)
            else -> defaultHandler.handle(error)
        }
    }

    private fun customError(error: DigitalPaymentsDetailedError, statusCode: Int) = error.let {
        jsonMapper.encodeToString(it)
    }.let { body ->
        APIGatewayProxyResponseEvent()
            .withStatusCode(statusCode)
            .withHeaders(
                mapOf(
                    CONTENT_TYPE to APPLICATION_JSON,
                    "X_Custom_Error" to "true"
                )
            )
            .also { response -> body.apply { response.withBody(this) } }
    }

}
