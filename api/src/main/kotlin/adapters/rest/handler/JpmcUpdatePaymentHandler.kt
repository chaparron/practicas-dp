package adapters.rest.handler

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import configuration.EnvironmentVariable
import domain.model.JpmcPaymentInformation
import domain.model.UpdatePaymentResponse
import domain.model.errors.DpErrorReason
import domain.model.errors.DpException
import domain.services.UpdatePaymentService
import java.math.BigDecimal
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import wabi.rest2lambda.RestHandler
import wabi.rest2lambda.ok
import java.util.*

class JpmcUpdatePaymentHandler(
    private val service: UpdatePaymentService,
    private val jsonMapper: Json,
    private val updatePaymentDummyEnabled: Boolean
) : RestHandler {

    companion object {
        private val logger = LoggerFactory.getLogger(JpmcUpdatePaymentHandler::class.java)
        const val PROCESS_INFORMATION_PATH = "/dp/jpmc/updatePayment"
    }


    override fun handleRequest(request: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent {
        logger.trace("Received request: [${request.body}]")

        return if (updatePaymentDummyEnabled) {
            ok(UpdatePaymentResponse(
                paymentId = Random().nextLong(),
                supplierOrderId = Random().nextLong(),
                amount = BigDecimal("100"),
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
                is DpException -> throw exception
                else -> throw DpException.from(reason = DpErrorReason.UNKNOWN, rootCause = exception)
            }
        }
}
