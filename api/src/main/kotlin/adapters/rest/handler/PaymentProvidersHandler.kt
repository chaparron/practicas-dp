package adapters.rest.handler

import adapters.rest.validations.RequestValidations.required
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import domain.services.providers.PaymentProviderService
import domain.services.state.StateValidatorService
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import wabi.rest2lambda.RestHandler
import wabi.rest2lambda.ok

class PaymentProvidersHandler(
    private val service: PaymentProviderService,
    private val stateValidatorService: StateValidatorService,
    private val jsonMapper: Json,
) : RestHandler {

    companion object {
        private val logger = LoggerFactory.getLogger(PaymentProvidersHandler::class.java)
        const val PAYMENT_PROVIDERS_PATH = "/dp/paymentProviders"
        const val SUPPLIER_ID_PARAM = "supplierId"
    }


    override fun handleRequest(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent {
        val supplierId = input.queryStringParameters[SUPPLIER_ID_PARAM]
            .required(SUPPLIER_ID_PARAM)
        stateValidatorService.validate(input)
        return ok(service.availableProviders(supplierId)
            .also {
                logger.trace("Payment Providers retrieved: $it")
            }
            .let {
                jsonMapper.encodeToString(it)
            }
        )
    }
}
