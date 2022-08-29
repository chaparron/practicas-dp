package adapters.rest.handler

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import configuration.EnvironmentVariable
import configuration.EnvironmentVariable.Companion.jpmcNotificationConfiguration
import org.slf4j.LoggerFactory
import wabi.rest2lambda.ok

/**
 * Test webhook for [REDIRECT_PATH]
 */
class RedirectWebhookHandler(
    private val configuration: EnvironmentVariable.JpmcNotificationConfiguration
): RequestHandler<APIGatewayProxyRequestEvent?, APIGatewayProxyResponseEvent> {

    companion object {
        private const val REDIRECT_PATH = "/dp/jpmc/notification"

        private val logger = LoggerFactory.getLogger(RedirectWebhookHandler::class.java)
    }

    constructor(): this(
        jpmcNotificationConfiguration()
    )

    override fun handleRequest(input: APIGatewayProxyRequestEvent?, context: Context?): APIGatewayProxyResponseEvent {
        return runCatching {
            logger.info("input=[$input]")
            ok(
                """
                    {"MerchantId":"${configuration.merchantId}","TerminalId":"${configuration.terminalId}", "BankId":"${configuration.bankId}", "Acknowledgement":"Received"}
                """.trimIndent()
            )
        }.onSuccess {
            logger.debug("response onSuccess=[$it]")
        }.onFailure {
            logger.error("thrown=[$it]", it)
        }.getOrThrow()
    }
}
