package configuration

import domain.functions.PaymentExpirationListener
import domain.functions.SupplierListenerFunction
import domain.services.CreatePaymentService
import domain.services.UpdatePaymentService
import domain.services.PaymentExpirationService
import domain.services.TokenProvider
import domain.services.state.StateValidatorService
import domain.services.providers.PaymentProviderService
import kotlinx.serialization.json.Json
import wabi2b.sdk.api.Wabi2bSdk
import java.time.Clock

interface Configuration {
    val jsonMapper: Json
    val createPaymentService: CreatePaymentService
    val jpmcStateValidationConfig: EnvironmentVariable.JpmcStateValidationConfig
    val supplierListenerFunction: SupplierListenerFunction
    val stateValidatorService: StateValidatorService
    val paymentProviderService: PaymentProviderService
    val updatePaymentService: UpdatePaymentService
    val wabi2bTokenProvider: TokenProvider
    val wabi2bSdk: Wabi2bSdk
    val clock: Clock
    val paymentExpirationService: PaymentExpirationService
    val paymentExpirationListener: PaymentExpirationListener
}
