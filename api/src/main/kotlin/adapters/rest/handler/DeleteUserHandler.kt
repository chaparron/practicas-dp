package adapters.rest.handler

import adapters.rest.validations.RequestValidations.required
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import domain.services.UserService
import org.slf4j.LoggerFactory
import wabi.rest2lambda.RestHandler
import wabi.rest2lambda.ok

class DeleteUserHandler(
    private val service: UserService,
) : RestHandler {

    companion object {
        private val logger = LoggerFactory.getLogger(DeleteUserHandler::class.java)
        const val USER_ID_PARAM = "userId"
        const val DELETE_USER_PATH = "/dp/user"
    }

    override fun handleRequest(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent {
        logger.info("About to delete user with id: ${input.body}")
        val userId = input.queryStringParameters[USER_ID_PARAM].required(USER_ID_PARAM).toLong()
        return ok(service.delete(userId).toString())
            .also {
                logger.trace("User deleted: $it")
            }
    }

}
