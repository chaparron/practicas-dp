package configuration

import adapters.rest.validations.Security
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import domain.services.SaleInformationService
import kotlinx.serialization.json.Json
import org.mockito.kotlin.mock

class TestConfiguration(
    override val jsonMapper: Json = MainConfiguration.jsonMapper,
    val authorizerWrapper: Security.AuthorizerWrapper = mock(),
    override val saleInformationService: SaleInformationService = mock(),

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

