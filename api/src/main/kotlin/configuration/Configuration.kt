package configuration

import domain.functions.SupplierListenerFunction
import domain.services.JpmcCreatePaymentService
import domain.services.state.StateValidatorService
import domain.services.providers.PaymentProviderService
import kotlinx.serialization.json.Json

interface Configuration {
    val jsonMapper: Json
    val jpmcCreatePaymentService: JpmcCreatePaymentService
    val jpmcStateValidationConfig: EnvironmentVariable.JpmcStateValidationConfig
    val supplierListenerFunction: SupplierListenerFunction
    val stateValidatorService: StateValidatorService
    val paymentProviderService: PaymentProviderService
}
