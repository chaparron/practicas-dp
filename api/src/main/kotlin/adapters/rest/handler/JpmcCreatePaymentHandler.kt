package adapters.rest.handler

import adapters.rest.validations.RequestValidations.required
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import domain.model.CreatePaymentRequest
import domain.model.errors.JpmcErrorReason
import domain.services.JpmcCreatePaymentService
import domain.services.state.StateValidatorService
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import wabi.rest2lambda.RestHandler
import wabi.rest2lambda.ok

class JpmcCreatePaymentHandler(
    private val service: JpmcCreatePaymentService,
    private val jsonMapper: Json,
    private val stateValidatorService: StateValidatorService,
) : RestHandler {

    companion object {
        const val CREATE_PAYMENT_PATH = "/dp/jpmc/createPayment"
    }

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun handleRequest(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent {
        stateValidatorService.validate(input)
        val request = jsonMapper.decodeFromString<CreatePaymentHandlerRequest>(input.body)
        return ok(service.createPayment(request.toCreatePaymentRequest())
            .also {
                logger.trace("Payment created: $it")
            }
            .let {
                jsonMapper.encodeToString(it)
            }
        )
    }

    private fun CreatePaymentHandlerRequest.toCreatePaymentRequest() = CreatePaymentRequest(
        supplierOrderId = supplierOrderId.required(JpmcErrorReason.MISSING_SUPPLIER_ID),
        amount = amount.required(JpmcErrorReason.MISSING_AMOUNT),
        totalAmount = totalAmount.required(JpmcErrorReason.MISSING_TOTAL_AMOUNT)
    )

    @Serializable
    class CreatePaymentHandlerRequest(
        val supplierOrderId: String,
        val amount: String,
        val totalAmount: String
    )
}
