package adapters.rest.handler

import adapters.rest.validations.RequestValidations.required
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import domain.services.SupplierOrderDelayService
import org.slf4j.LoggerFactory
import wabi.rest2lambda.RestHandler
import wabi.rest2lambda.ok

class SupplierOrderDelayHandler(
    private val service: SupplierOrderDelayService,
//    private val stateValidatorService: StateValidatorService,
//    private val jsonMapper: Json,
) : RestHandler {

    companion object {
        private val logger = LoggerFactory.getLogger(SupplierOrderDelayHandler::class.java)
        const val SUPPLIER_ORDER_ID_PARAM = "supplierOrderId"
        const val SUPPLIER_ORDER_DELAY_PATH = "/dp/supplierOrderDelay"
    }

    override fun handleRequest(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent {
        val supplierOrderId =
            input.queryStringParameters[SUPPLIER_ORDER_ID_PARAM].required(SUPPLIER_ORDER_ID_PARAM).toLong()
//        val state = stateValidatorService.getState(input)
        return ok(service.isDelayed(supplierOrderId).toString())
            .also {
                logger.trace("Supplier Order delay retrieved: $it")
            }
    }
}
