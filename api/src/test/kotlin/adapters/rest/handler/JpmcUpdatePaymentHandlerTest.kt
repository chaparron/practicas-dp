package adapters.rest.handler

import apiGatewayEventRequest
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import domain.model.JpmcPaymentInformation
import domain.model.UpdatePaymentResponse
import domain.services.JpmcUpdatePaymentService
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpMethod
import randomString

@ExtendWith(MockitoExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JpmcUpdatePaymentHandlerTest {

    private var context: Context = mock()
    private var service: JpmcUpdatePaymentService = mock()
    private var jsonMapper: Json = Json { ignoreUnknownKeys = true }

    private lateinit var sut: JpmcUpdatePaymentHandler

    companion object {
        private const val PATH = "/dp/jpmc/updatePayment"
        private const val ACCESS_TOKEN = "my-token"

        private const val PAYMENT_ID = "X"
        private const val SUPPLIER_ORDER_ID = "X"
        private const val AMOUNT = "X"
        private const val TOTAL_AMOUNT = "X"
        private const val RESPONSE_CODE = "X"
        private const val MESSAGE = "X"

        private const val EXPECTED_JSON_RESPONSE =
            "{\"paymentId\":\"${PAYMENT_ID}\",\"supplierOrderId\":\"${SUPPLIER_ORDER_ID}\",\"amount\":\"${AMOUNT}\",\"responseCode\":\"${RESPONSE_CODE}\",\"message\":\"${MESSAGE}\"}"

    }

    @BeforeEach
    fun setUp() {
        sut = JpmcUpdatePaymentHandler(service, jsonMapper)
    }

    @Test
    fun `given valid encData when updatePayment then return valid response `() {
        val request = anyJpmcPaymentInformation()

        whenever(service.update(request))
            .thenReturn(
                UpdatePaymentResponse(
                    PAYMENT_ID,
                    SUPPLIER_ORDER_ID,
                    AMOUNT,
                    RESPONSE_CODE,
                    MESSAGE
                )
            )

        val response = sut.handleRequest(anyApiGatewayProxyRequestEvent(request), context)

        assertEquals(EXPECTED_JSON_RESPONSE, response.body)

        verify(service).update(request)
    }

    private fun anyJpmcPaymentInformation() = JpmcPaymentInformation(
        encData = randomString()
    )

    private fun anyApiGatewayProxyRequestEvent(
        request: JpmcPaymentInformation = anyJpmcPaymentInformation()
    ): APIGatewayProxyRequestEvent = apiGatewayEventRequest(
        method = HttpMethod.POST,
        path = PATH,
        authorization = ACCESS_TOKEN,
        body = """
            {"encData":"${request.encData}"}
        """.trimIndent()
    )
}
