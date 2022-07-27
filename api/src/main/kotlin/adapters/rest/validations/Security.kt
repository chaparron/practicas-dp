package adapters.rest.validations

import adapters.rest.authorizer
import adapters.rest.validations.Security.SecurityCheckResult.*
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent

open class Security {

    companion object {
        const val AUTH_FE_WEB = "FE_WEB"
    }

    data class ForbiddenException(val errors: List<SecurityError>) : RuntimeException("Forbidden request") {
        override val message: String = errors.map { it.message }.joinToString { "\n" }
    }

    sealed interface SecurityCheckResult {
        object SecurityIgnored : SecurityCheckResult
        object SecuritySuccess : SecurityCheckResult
        data class SecurityError(val message: String) : SecurityCheckResult
    }

    open class AuthorizerWrapper(event: APIGatewayProxyRequestEvent) {
        private val authorizer = event.authorizer()

        fun requestedByUser(userId: String) = authorizer.getLoggedUser()?.id == userId
        fun matchAnyAuthority(authorities: List<String>) = authorities.any { authorizer.hasAuthority(it) }
        fun getState() = authorizer.getRawToken()?.let { JwtDecoder(it).getStateField() }
    }

    open fun buildAuthorizer(event: APIGatewayProxyRequestEvent) = AuthorizerWrapper(event)

    fun requiresAny(userAuthorities: List<String> = emptyList(), genericAuthorities: List<String> = emptyList()) =
        { event: APIGatewayProxyRequestEvent, userId: String? ->
            val userValidationsResult = if (userAuthorities.isEmpty()) SecurityIgnored else validateUserAuthorities(
                userAuthorities,
                event,
                userId
            )
            val genericValidationsResult =
                if (genericAuthorities.isEmpty()) SecurityIgnored else validateGenericAuthorities(
                    userAuthorities,
                    event
                )
            val results = listOf(userValidationsResult, genericValidationsResult)
            if (!results.any { it is SecuritySuccess }) {
                throw ForbiddenException(results.filterIsInstance<SecurityError>())
            }
        }

    private fun validateUserAuthorities(
        authorities: List<String>,
        event: APIGatewayProxyRequestEvent,
        userId: String?
    ): SecurityCheckResult =
        if (userId == null) {
            SecurityError("User is required for validate the following authorities: $authorities")
        } else {
            val authorizer = buildAuthorizer(event)
            if (!authorizer.requestedByUser(userId) || !authorizer.matchAnyAuthority(authorities)) {
                SecurityError("User $userId requires at least one of the following authorities: $authorities ")
            } else SecuritySuccess
        }

    private fun validateGenericAuthorities(
        authorities: List<String>,
        event: APIGatewayProxyRequestEvent
    ): SecurityCheckResult {
        val authorizer = buildAuthorizer(event)
        return if (!authorizer.matchAnyAuthority(authorities)) {
            SecurityError("At least one of the following authorities are required: $authorities ")
        } else SecuritySuccess
    }
}
