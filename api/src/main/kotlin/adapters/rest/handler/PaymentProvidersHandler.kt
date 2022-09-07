package adapters.rest.handler

import adapters.rest.validations.RequestValidations.required
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import domain.services.state.StateValidatorService
import domain.services.providers.PaymentProviderService
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
        const val EMPTY_LIST_BODY = "[]"
    }


    override fun handleRequest(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent {
        val supplierId = input.queryStringParameters[SUPPLIER_ID_PARAM]
            .required(SUPPLIER_ID_PARAM).toLong()
        val state = stateValidatorService.getState(input)
        return if(stateValidatorService.validate(state)) {
            ok(service.availableProviders(supplierId)
                .let {
                    jsonMapper.encodeToString(it)
                }
            )
        }
        else {
            ok(EMPTY_LIST_BODY)
        }.also {
            logger.trace("Payment Providers retrieved: $it")
        }
    }
}
