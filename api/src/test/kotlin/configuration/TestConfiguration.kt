package configuration

import adapters.rest.validations.Security
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import domain.functions.BankAccountListenerFunction
import domain.services.ISaleInformationService
import kotlinx.serialization.json.Json
import org.mockito.kotlin.mock

class TestConfiguration(
    override val jsonMapper: Json = MainConfiguration.jsonMapper,
    val authorizerWrapper: Security.AuthorizerWrapper = mock(),
    override val saleInformationService: ISaleInformationService = mock(),
    override val bankAccountListenerFunction: BankAccountListenerFunction = BankAccountListenerFunction(jsonMapper, mock()),

    ) : Configuration {

    companion object {
        fun mockedInstance() = TestConfiguration()
    }

    override val security = object : Security() {
        override fun buildAuthorizer(event: APIGatewayProxyRequestEvent): AuthorizerWrapper {
            return authorizerWrapper
        }
    }

    init {
        EnvironmentVariable
            .values()
            .forEach { it.overrideSetting(it.name.lowercase()) }
    }

}

