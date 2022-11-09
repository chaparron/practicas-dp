package adapters.rest.handler


import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import domain.model.Role
import domain.model.User
import domain.services.UserService
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import wabi.rest2lambda.RestHandler
import wabi.rest2lambda.ok

class UpdateUserHandler(
    private val service: UserService,
    private val jsonMapper: Json
) : RestHandler {

    companion object {
        private val logger = LoggerFactory.getLogger(UpdateUserHandler::class.java)
        const val USER_PATH = "/dp/user"
    }

    override fun handleRequest(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent {
        val request = jsonMapper.decodeFromString<UpdateUserHandlerRequest>(input.body)
        return ok(service.update(request.toUserRequest())
            .also {
                logger.trace("User updated: $it")
            }
            .let {
                jsonMapper.encodeToString(it.toUpdateUserHandlerResponse())
            }
        )
    }

    private fun UpdateUserHandlerRequest.toUserRequest() = User(
        name,
        userId,
        mail,
        country,
        active,
        phone,
        role,
        createdAt,
        lastLogin,
        orders
    )

    private fun User.toUpdateUserHandlerResponse() = UpdateUserHandlerResponse(
        name,
        userId,
        mail,
        country,
        active,
        phone,
        role,
        createdAt,
        lastLogin,
        orders
    )

}

@Serializable
data class UpdateUserHandlerRequest(
    val name: String,
    val userId: Long,
    val mail: String,
    val country: String,
    val active: Boolean,
    val phone: String,
    val role: Role,
    val createdAt: String,
    val lastLogin: String,
    val orders: List<String>
)

@Serializable
data class UpdateUserHandlerResponse(
    val name: String,
    val userId: Long,
    val mail: String,
    val country: String,
    val active: Boolean,
    val phone: String,
    val role: Role,
    val createdAt: String,
    val lastLogin: String,
    val orders: List<String>
)
