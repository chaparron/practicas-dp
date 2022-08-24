package adapters.rest.handler

import adapters.rest.validations.RequestValidations.required
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import configuration.EnvironmentVariable
import domain.model.errors.DpErrorReason
import domain.services.state.StateValidatorService
import domain.services.providers.PaymentProviderService
import domain.services.providers.Provider
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
        // FIXME Delete mock as soon possible
        return if (EnvironmentVariable.jpmcProvidersDummyEnabled().toBoolean()) {
            ok(listOf(Provider.JP_MORGAN)
                .also {
                    logger.warn("Provider Mock enabled!")
                }
                .let {
                    jsonMapper.encodeToString(it)
                })
        } else {
            val supplierId = input.queryStringParameters[SUPPLIER_ID_PARAM]
                .required(DpErrorReason.MISSING_SUPPLIER_ID)

            val state = stateValidatorService.getState(input)

            ok(service.availableProviders(state, supplierId)
                .also {
                    logger.trace("Payment Providers retrieved: $it")
                }
                .let {
                    jsonMapper.encodeToString(it)
                }
            )
        }

    }
}
