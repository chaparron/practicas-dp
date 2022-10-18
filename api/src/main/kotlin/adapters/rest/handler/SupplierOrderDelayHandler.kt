package adapters.rest.handler

import adapters.rest.validations.RequestValidations.required
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import domain.services.DefaultSupplierOrderDelayService
import org.slf4j.LoggerFactory
import wabi.rest2lambda.RestHandler
import wabi.rest2lambda.ok

class SupplierOrderDelayHandler(
    private val service: DefaultSupplierOrderDelayService,
//    private val stateValidatorService: StateValidatorService,
//    private val jsonMapper: Json,
) : RestHandler {

    companion object {
        private val logger = LoggerFactory.getLogger(SupplierOrderDelayHandler::class.java)
        const val SUPPLIER_ORDER_ID_PARAM = "supplierOrderID"
        const val SUPPLIER_ORDER_DELAY_PATH = "/dp/supplierOrderDelay"
    }

    override fun handleRequest(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent {
        val supplierOrderID =
            input.queryStringParameters[SUPPLIER_ORDER_ID_PARAM].required(SUPPLIER_ORDER_ID_PARAM).toLong()
//        val state = stateValidatorService.getState(input)
        return (ok(if (service.isDelayed(supplierOrderID)) "Order arrived delayed" else "Order arrived on time"))
            .also {
                logger.trace("Payment Providers retrieved: $it")
            }
    }
}
