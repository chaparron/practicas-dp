package configuration

import domain.functions.SupplierListenerFunction
import domain.services.JpmcSaleInformationService
import domain.services.state.StateValidatorService
import domain.services.providers.PaymentProviderService
import kotlinx.serialization.json.Json

interface Configuration {
    val jsonMapper: Json
    val jpmcSaleInformationService: JpmcSaleInformationService
    val jpmcStateValidationConfig: EnvironmentVariable.JpmcStateValidationConfig
    val supplierListenerFunction: SupplierListenerFunction
    val stateValidatorService: StateValidatorService
    val paymentProviderService: PaymentProviderService
}
