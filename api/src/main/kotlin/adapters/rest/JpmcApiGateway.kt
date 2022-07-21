package adapters.rest

import adapters.rest.handler.JpmcPaymentMethodsHandler
import adapters.rest.handler.JpmcPaymentMethodsHandler.Companion.PAYMENT_METHODS_PATH
import adapters.rest.handler.JpmcProcessInformationHandler
import adapters.rest.handler.JpmcProcessInformationHandler.Companion.PROCESS_INFORMATION_PATH
import adapters.rest.handler.JpmcSaleInformationHandler
import adapters.rest.handler.JpmcSaleInformationHandler.Companion.SALE_INFORMATION_PATH
import adapters.rest.validations.Security
import configuration.Configuration
import configuration.MainConfiguration
import wabi.rest2lambda.ApiGatewayProxy
import wabi.rest2lambda.RestMappings

class JpmcApiGateway(
    private val configuration: Configuration = MainConfiguration
) : ApiGatewayProxy() {

    private val security = configuration.security

    init {
        initialize()
    }

    override fun setupMappings(builder: RestMappings.Builder): RestMappings.Builder = builder
        .get(
            path = SALE_INFORMATION_PATH,
            handler = JpmcSaleInformationHandler(
                service = configuration.saleInformationService,
                jsonMapper = configuration.jsonMapper,
                securityCheck = security.requiresAny(userAuthorities = listOf(Security.AUTH_FE_WEB))
            )
        )
        .post(
            path = PROCESS_INFORMATION_PATH,
            handler = JpmcProcessInformationHandler()
        )
        .get(
            path = PAYMENT_METHODS_PATH,
            handler = JpmcPaymentMethodsHandler()
        )
}
