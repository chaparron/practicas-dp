package configuration

import adapters.rest.validations.Security
import domain.functions.SupplierListenerFunction
import domain.services.JpmcSaleInformationService
import domain.services.state.StateValidatorService
import domain.services.providers.PaymentProviderService
import kotlinx.serialization.json.Json
import org.mockito.kotlin.mock

class TestConfiguration(
    override val jsonMapper: Json = MainConfiguration.jsonMapper,
    val authorizerWrapper: Security.AuthorizerWrapper = mock(),
    override val jpmcSaleInformationService: JpmcSaleInformationService = mock(),
    override val jpmcStateValidationConfig: EnvironmentVariable.JpmcStateValidationConfig = mock(),
    override val supplierListenerFunction: SupplierListenerFunction = SupplierListenerFunction(jsonMapper, mock()),
    override val stateValidatorService: StateValidatorService = mock(),
    override val paymentProviderService: PaymentProviderService = mock()
) : Configuration {

    companion object {
        fun mockedInstance() = TestConfiguration()
    }

    init {
        EnvironmentVariable
            .values()
            .forEach { it.overrideSetting(it.name.lowercase()) }
    }

}
