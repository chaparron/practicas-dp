package configuration

import adapters.rest.validations.Security
import domain.functions.PaymentExpirationListener
import domain.functions.SupplierListenerFunction
import domain.functions.SupplierOrderDelayListener
import domain.services.*
import domain.services.state.StateValidatorService
import domain.services.providers.PaymentProviderService
import kotlinx.serialization.json.Json
import org.mockito.kotlin.mock
import wabi2b.sdk.api.Wabi2bSdk
import java.time.Clock

class TestConfiguration(
    override val jsonMapper: Json = MainConfiguration.jsonMapper,
    val authorizerWrapper: Security.AuthorizerWrapper = mock(),
    override val createPaymentService: CreatePaymentService = mock(),
    override val jpmcStateValidationConfig: EnvironmentVariable.JpmcStateValidationConfig = mock(),
    override val supplierListenerFunction: SupplierListenerFunction = SupplierListenerFunction(jsonMapper, mock()),
    override val paymentExpirationListener: PaymentExpirationListener = PaymentExpirationListener(mock(), jsonMapper),
    override val stateValidatorService: StateValidatorService = mock(),
    override val paymentProviderService: PaymentProviderService = mock(),
    override val updatePaymentService: UpdatePaymentService = mock(),
    override val wabi2bTokenProvider: TokenProvider = mock(),
    override val wabi2bSdk: Wabi2bSdk = mock(),
    override val clock: Clock = mock(),
    override val paymentExpirationService: PaymentExpirationService = mock(),
    override val supplierOrderDelayService: DefaultSupplierOrderDelayService = mock(),
    override val supplierOrderDelayListener: SupplierOrderDelayListener = mock()
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
