package adapters.rest.handler

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import wabi.rest2lambda.RestHandler

class JpmcPaymentMethodsHandler() : RestHandler {

    companion object {
        const val PAYMENT_METHODS_PATH = "/jpmc/paymentMethods"
    }

    override fun handleRequest(input: APIGatewayProxyRequestEvent?, context: Context?): APIGatewayProxyResponseEvent {
        TODO("Not yet implemented")
    }
}
