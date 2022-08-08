package adapters.rest.handler

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import configuration.EnvironmentVariable
import domain.model.JpmcPaymentInformation
import domain.model.UpdatePaymentResponse
import domain.model.errors.JpmcErrorReason
import domain.model.errors.JpmcException
import domain.services.JpmcUpdatePaymentService
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import wabi.rest2lambda.RestHandler
import wabi.rest2lambda.ok
import java.util.*

class JpmcUpdatePaymentHandler(
    private val service: JpmcUpdatePaymentService,
    private val jsonMapper: Json,
) : RestHandler {

    companion object {
        const val PROCESS_INFORMATION_PATH = "/dp/jpmc/updatePayment"
    }

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun handleRequest(request: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent {
        logger.trace("Received request: [${request.body}]")

        return if (EnvironmentVariable.jpmcUpdatePaymentDummyEnabled().toBoolean()) {
            ok(UpdatePaymentResponse(
                paymentId = UUID.randomUUID().toString(),
                supplierOrderId = "666",
                amount = "100",
                totalAmount = "100",
                responseCode = "00",
                message = "Transaction Successful"
            ).also {
                logger.trace("Payment Updated: $it")
            }
                .let {
                    jsonMapper.encodeToString(it)
                })

        } else {
            ok(service.update(map(request))
                .also {
                    logger.trace("Payment Updated: $it")
                }
                .let {
                    jsonMapper.encodeToString(it)
                }
            )
        }


    }

    private fun map(request: APIGatewayProxyRequestEvent): JpmcPaymentInformation =
        runCatching {
            jsonMapper.decodeFromString<JpmcPaymentInformation>(request.body)
        }.getOrElse { exception ->
            when (exception) {
                is JpmcException -> throw exception
                else -> throw JpmcException.from(reason = JpmcErrorReason.UNKNOWN, rootCause = exception)
            }
        }
}
