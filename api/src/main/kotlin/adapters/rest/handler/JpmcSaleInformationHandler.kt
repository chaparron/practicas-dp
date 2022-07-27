package adapters.rest.handler

import adapters.rest.validations.RequestValidations.required
import adapters.rest.validations.Security
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import configuration.EnvironmentVariable
import domain.model.errors.FunctionalityNotAvailable
import domain.model.errors.JpmcErrorReason
import domain.services.SaleInformationService
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import wabi.rest2lambda.RestHandler
import wabi.rest2lambda.ok

class JpmcSaleInformationHandler(
    private val service: SaleInformationService,
    private val jsonMapper: Json,
    private val stateValidationConfig: EnvironmentVariable.JpmcStateValidationConfig,
    private val security: Security
) : RestHandler {

    companion object {
        const val SALE_INFORMATION_PATH = "/jpmc/saleInformation"
        const val AMOUNT_PARAM = "amount"
    }

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun handleRequest(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent {
        val amount = input.queryStringParameters[AMOUNT_PARAM]
            .required(JpmcErrorReason.MISSING_AMOUNT)


        if (stateValidationConfig.enabled) {
            val state = security.buildAuthorizer(input).getState()
            logger.trace("state=$state")

            if (!stateValidationConfig.availableFor.contains(state)) {
                throw FunctionalityNotAvailable()
            }
        }

        return ok(service.getSaleInformation(amount)
            .also {
                logger.trace("Sale information retrieved: $it")
            }
            .let {
                jsonMapper.encodeToString(it)
            }
        )

    }
}
