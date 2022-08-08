package adapters.rest

import adapters.rest.handler.JpmcUpdatePaymentHandler
import adapters.rest.handler.JpmcUpdatePaymentHandler.Companion.PROCESS_INFORMATION_PATH
import adapters.rest.handler.JpmcCreatePaymentHandler
import adapters.rest.handler.JpmcCreatePaymentHandler.Companion.CREATE_PAYMENT_PATH
import adapters.rest.handler.PaymentProvidersHandler
import adapters.rest.handler.PaymentProvidersHandler.Companion.PAYMENT_PROVIDERS_PATH
import adapters.rest.handler.RestErrorHandler
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
            handler = JpmcCreatePaymentHandler(
                service = configuration.jpmcCreatePaymentService,
                jsonMapper = configuration.jsonMapper,
                stateValidatorService = configuration.stateValidatorService
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
                service = configuration.jpmcUpdatePaymentService,
                jsonMapper = configuration.jsonMapper
            )
        )

    override fun getErrorHandler(): ErrorHandler {
        return RestErrorHandler(configuration.jsonMapper)
    }
}
