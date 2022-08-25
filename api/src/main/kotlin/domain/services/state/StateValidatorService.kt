package domain.services.state

import adapters.rest.validations.Security
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import configuration.EnvironmentVariable
import domain.model.errors.FunctionalityNotAvailable
import org.slf4j.LoggerFactory
import wabi2b.dtos.customers.shared.CustomerDto
import wabi2b.sdk.customers.customer.CustomersSdk

class StateValidatorService(
    private val stateValidationConfig: EnvironmentVariable.JpmcStateValidationConfig,
    private val security: Security,
    private val customersSdk: CustomersSdk
) {

    companion object {
        private val logger = LoggerFactory.getLogger(StateValidatorService::class.java)
        private const val EMPTY_STATE = ""
        private const val DELIVERY_ADDRESS_TYPE = "DELIVERY"
    }

    fun getState(input: APIGatewayProxyRequestEvent): String {
        if (stateValidationConfig.enabled) {
            return security.buildAuthorizer(input).getToken(input)
                .runCatching {
                    customersSdk.myProfile(this)
                        .let {
                            it.addresses.first { address ->
                                address.preferred && address.addressType ==  DELIVERY_ADDRESS_TYPE
                            }.state?.id ?: throw CustomerWithoutStateException(it)
                        }
                }.onFailure {
                    logger.error("There was on error retrieving customer address for this token: $it")
                }.onSuccess {
                    logger.info("Customer state retrieved successfully: $it")
                }.getOrThrow()
        }
        return EMPTY_STATE
    }
    data class CustomerWithoutStateException(val customer: CustomerDto): RuntimeException("The following customer doesn't have state: $customer")
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
