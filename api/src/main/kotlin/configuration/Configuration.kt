package configuration

import domain.functions.PaymentExpirationListener
import domain.functions.SupplierListenerFunction
import domain.functions.SupplierOrderDelayListener
import domain.functions.UserListener
import domain.services.*
import domain.services.providers.PaymentProviderService
import domain.services.state.StateValidatorService
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
    val supplierOrderDelayService: SupplierOrderDelayService
    val updatePaymentService: UpdatePaymentService
    val wabi2bTokenProvider: TokenProvider
    val wabi2bSdk: Wabi2bSdk
    val clock: Clock
    val paymentExpirationService: PaymentExpirationService
    val paymentExpirationListener: PaymentExpirationListener
    val userListener: UserListener
    val userService: UserService
}
