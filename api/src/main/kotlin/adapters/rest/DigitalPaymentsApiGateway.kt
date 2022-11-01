package adapters.rest

import adapters.rest.handler.*
import adapters.rest.handler.JpmcUpdatePaymentHandler.Companion.UPDATE_PAYMENT_PATH
import adapters.rest.handler.CreatePaymentHandler.Companion.CREATE_PAYMENT_PATH
import adapters.rest.handler.PaymentProvidersHandler.Companion.PAYMENT_PROVIDERS_PATH
import adapters.rest.handler.SupplierOrderDelayHandler.Companion.SUPPLIER_ORDER_DELAY_PATH
import adapters.rest.handler.UserHandler.Companion.USER_PATH
import configuration.Configuration
import configuration.EnvironmentVariable
import configuration.MainConfiguration
import wabi.rest2lambda.ApiGatewayProxy
import wabi.rest2lambda.ErrorHandler
import wabi.rest2lambda.RestMappings

class DigitalPaymentsApiGateway(
    private val configuration: Configuration = MainConfiguration
) : ApiGatewayProxy() {
    init {
        initialize()
    }

    override fun setupMappings(builder: RestMappings.Builder): RestMappings.Builder = builder
        .post(
            path = CREATE_PAYMENT_PATH,
            handler = CreatePaymentHandler(
                service = configuration.createPaymentService,
                jsonMapper = configuration.jsonMapper,
                stateValidatorService = configuration.stateValidatorService,
                createPaymentDummyEnabled = EnvironmentVariable.jpmcCreatePaymentDummyEnabled().toBoolean()
            )
        )
        .get(
            path = PAYMENT_PROVIDERS_PATH,
            handler = PaymentProvidersHandler(
                service = configuration.paymentProviderService,
                stateValidatorService = configuration.stateValidatorService,
                jsonMapper = configuration.jsonMapper
            )
        )
        .get(
            path = SUPPLIER_ORDER_DELAY_PATH,
            handler = SupplierOrderDelayHandler(
                service = configuration.supplierOrderDelayService,
//                stateValidatorService = configuration.stateValidatorService,
//                jsonMapper = configuration.jsonMapper
            )
        )
        .get(
            path = USER_PATH,
            handler = UserHandler(
                service = configuration.userService
            )
        )
        .post(
            path = UPDATE_PAYMENT_PATH,
            handler = JpmcUpdatePaymentHandler(
                service = configuration.updatePaymentService,
                jsonMapper = configuration.jsonMapper,
                updatePaymentDummyEnabled = EnvironmentVariable.jpmcUpdatePaymentDummyEnabled().toBoolean()
            )
        )

    override fun getErrorHandler(): ErrorHandler {
        return ErrorHandler(configuration.jsonMapper)
    }
}
