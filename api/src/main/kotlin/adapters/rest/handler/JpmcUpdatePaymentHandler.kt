package adapters.rest.handler

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import wabi.rest2lambda.RestHandler

class JpmcUpdatePaymentHandler : RestHandler {

    companion object {
        const val PROCESS_INFORMATION_PATH = "/dp/jpmc/updatePayment"
    }

    override fun handleRequest(input: APIGatewayProxyRequestEvent?, context: Context?): APIGatewayProxyResponseEvent {
        TODO("Not yet implemented")
    }
}
