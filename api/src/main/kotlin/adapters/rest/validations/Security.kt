package adapters.rest.validations

import adapters.rest.authorizer
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent

open class Security {

    open class AuthorizerWrapper(event: APIGatewayProxyRequestEvent) {
        private val authorizer = event.authorizer()

        fun requestedByUser(userId: String) = authorizer.getLoggedUser()?.id == userId
        fun matchAnyAuthority(authorities: List<String>) = authorities.any { authorizer.hasAuthority(it) }
        fun getToken(event: APIGatewayProxyRequestEvent) =
            authorizer.getRawToken() ?: throw UnauthorizedException(event = event.toString())
    }

    data class UnauthorizedException(val event: String) : RuntimeException("Can't get valid token from event: $event")

    open fun buildAuthorizer(event: APIGatewayProxyRequestEvent) = AuthorizerWrapper(event)

}
