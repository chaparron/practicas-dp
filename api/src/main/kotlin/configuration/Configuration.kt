package configuration

import domain.functions.SupplierListenerFunction
import domain.services.JpmcCreatePaymentService
import domain.services.JpmcUpdatePaymentService
import domain.services.PaymentExpirationService
import domain.services.TokenProvider
import domain.services.state.StateValidatorService
import domain.services.providers.PaymentProviderService
import kotlinx.serialization.json.Json
import wabi2b.sdk.api.Wabi2bSdk
import java.time.Clock

interface Configuration {
    val jsonMapper: Json
    val jpmcCreatePaymentService: JpmcCreatePaymentService
    val jpmcStateValidationConfig: EnvironmentVariable.JpmcStateValidationConfig
    val supplierListenerFunction: SupplierListenerFunction
    val stateValidatorService: StateValidatorService
    val paymentProviderService: PaymentProviderService
    val jpmcUpdatePaymentService: JpmcUpdatePaymentService
    val wabi2bTokenProvider: TokenProvider
    val wabi2bSdk: Wabi2bSdk
    val clock: Clock
    val paymentExpirationService: PaymentExpirationService
}
