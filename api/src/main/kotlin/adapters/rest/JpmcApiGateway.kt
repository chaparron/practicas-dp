package adapters.rest

import adapters.rest.handler.JpmcProcessInformationHandler
import adapters.rest.handler.JpmcProcessInformationHandler.Companion.PROCESS_INFORMATION_PATH
import adapters.rest.handler.JpmcSaleInformationHandler
import adapters.rest.handler.JpmcSaleInformationHandler.Companion.SALE_INFORMATION_PATH
import adapters.rest.handler.PaymentProvidersHandler
import adapters.rest.handler.PaymentProvidersHandler.Companion.PAYMENT_PROVIDERS_PATH
import configuration.Configuration
import configuration.MainConfiguration
import wabi.rest2lambda.ApiGatewayProxy
import wabi.rest2lambda.RestMappings

class JpmcApiGateway(
    private val configuration: Configuration = MainConfiguration
) : ApiGatewayProxy() {
    init {
        initialize()
    }

    override fun setupMappings(builder: RestMappings.Builder): RestMappings.Builder = builder
        .get(
            path = SALE_INFORMATION_PATH,
            handler = JpmcSaleInformationHandler(
                service = configuration.jpmcSaleInformationService,
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
            handler = JpmcProcessInformationHandler()
        )
}
