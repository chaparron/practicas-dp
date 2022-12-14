package adapters.rest.handler

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import configuration.EnvironmentVariable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import randomString
import wabi.rest2lambda.ok
import kotlin.test.assertEquals

class RedirectWebhookHandlerTest {

    companion object {
        private val config = EnvironmentVariable.JpmcNotificationConfiguration(
            bankId = randomString(),
            terminalId = randomString(),
            merchantId = randomString(),
            mcc = randomString()
        )

        private val expectedBody = """
                    {"MerchantId":"${config.merchantId}","TerminalId":"${config.terminalId}", "BankId":"${config.bankId}", "Acknowledgement":"Received"}
                """.trimIndent()
    }

    private lateinit var sut: RedirectWebhookHandler

    @BeforeEach
    fun setup() {
        sut = RedirectWebhookHandler(config)
    }

    @Test
    fun `return a fixed response`() {
        //Given
        val expected = ok(body = expectedBody)

        //When
        val actual = sut.handleRequest(APIGatewayProxyRequestEvent(), null)

        //Then
        assertEquals(expected, actual)
    }

}
