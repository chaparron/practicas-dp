package adapters.rest

import adapters.rest.handler.*
import adapters.rest.handler.JpmcUpdatePaymentHandler.Companion.PROCESS_INFORMATION_PATH
import adapters.rest.handler.CreatePaymentHandler.Companion.CREATE_PAYMENT_PATH
import adapters.rest.handler.PaymentProvidersHandler.Companion.PAYMENT_PROVIDERS_PATH
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
        .post(
            path = PROCESS_INFORMATION_PATH,
            handler = JpmcUpdatePaymentHandler(
                service = configuration.updatePaymentService,
                jsonMapper = configuration.jsonMapper,
                updatePaymentDummyEnabled = EnvironmentVariable.jpmcUpdatePaymentDummyEnabled().toBoolean()
            )
        )

    override fun getErrorHandler(): ErrorHandler {
        return RestErrorHandler(configuration.jsonMapper)
    }
}
