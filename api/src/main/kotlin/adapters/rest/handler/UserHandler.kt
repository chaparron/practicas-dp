package adapters.rest.handler

import adapters.rest.validations.RequestValidations.required
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import domain.services.UserService
import org.slf4j.LoggerFactory
import wabi.rest2lambda.RestHandler
import wabi.rest2lambda.ok

class UserHandler (
    private val service: UserService
) : RestHandler {

    companion object{
        private val logger = LoggerFactory.getLogger(UserHandler::class.java)
        const val USER_ID_PARAM = "userId"
        const val USER_PATH = "/dp/user"
    }
    override fun handleRequest(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent {
        val userId = input.queryStringParameters[USER_ID_PARAM].required(USER_ID_PARAM).toLong()
        return ok(service.get(userId).toString())
            .also{
                logger.trace("User retrived: $it")
            }
    }

}
