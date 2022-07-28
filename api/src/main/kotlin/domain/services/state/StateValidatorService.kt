package domain.services.state

import adapters.rest.validations.Security
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import configuration.EnvironmentVariable
import domain.model.errors.FunctionalityNotAvailable
import org.slf4j.LoggerFactory

class StateValidatorService(
    private val stateValidationConfig: EnvironmentVariable.JpmcStateValidationConfig,
    private val security: Security
) {

    companion object {
        private const val EMPTY_STATE = ""
    }

    private val logger = LoggerFactory.getLogger(javaClass)

    fun getState(input: APIGatewayProxyRequestEvent): String {
        var state = EMPTY_STATE
        if (stateValidationConfig.enabled) {
            state =
                security.buildAuthorizer(input).getState().takeIf { it != null } ?: throw FunctionalityNotAvailable()
            logger.trace("state=$state")
        }
        return state
    }

    fun validate(input: APIGatewayProxyRequestEvent) {
        if (!validate(getState(input)))
            throw FunctionalityNotAvailable()
    }

    fun validate(state: String): Boolean =
        if (stateValidationConfig.enabled) {
            stateValidationConfig.availableFor.contains(state)
        } else {
            true
        }

}
