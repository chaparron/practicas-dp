package adapters.rest.handler

import anyCreatePaymentRequest
import apiGatewayEventRequest
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import domain.model.CreatePaymentRequest
import domain.model.CreatePaymentResponse
import domain.model.errors.FunctionalityNotAvailable
import domain.services.JpmcCreatePaymentService
import domain.services.state.StateValidatorService
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.http.HttpMethod
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JpmcCreatePaymentHandlerTest {

    private var context: Context = mock()
    private var service: JpmcCreatePaymentService = mock()
    private var jsonMapper: Json = Json { ignoreUnknownKeys = true }
    private var stateValidatorService: StateValidatorService = mock()

    private lateinit var sut: JpmcCreatePaymentHandler

    companion object {
        private const val PATH = "/dp/jpmc/createPayment"
        private const val ACCESS_TOKEN = "my-token"

        private const val BANK_ID = "X"
        private const val MERCHANT_ID = "X"
        private const val TERMINAL_ID = "X"
        private const val ENC_DATA = "X"
        private const val AMOUNT_KEY = "amount"
        private const val AMOUNT = "12345"

        private const val EXPECTED_JSON_RESPONSE =
            "{\"bankId\":\"$BANK_ID\",\"merchantId\":\"$MERCHANT_ID\",\"terminalId\":\"$TERMINAL_ID\",\"encData\":\"$ENC_DATA\"}"
    }

    @BeforeEach
    fun setUp() {
        Mockito.reset(service, stateValidatorService)
        sut = JpmcCreatePaymentHandler(service, jsonMapper, stateValidatorService)
    }

    @Test
    fun `given state validation ok when createPayment then return valid response `() {
        val request = anyCreatePaymentRequest()

        whenever(service.createPayment(any()))
            .thenReturn(CreatePaymentResponse(BANK_ID, MERCHANT_ID, TERMINAL_ID, ENC_DATA))

        val response = sut.handleRequest(anyApiGatewayProxyRequestEvent(request), context)

        assertEquals(EXPECTED_JSON_RESPONSE, response.body)

        verify(stateValidatorService, times(1)).validate(any<APIGatewayProxyRequestEvent>())
        verify(service).createPayment(request)
    }

    @Test
    fun `given state validation ko when createPayment then throw custom exception FunctionalityNotAvailable`() {
        whenever(stateValidatorService.validate(any<APIGatewayProxyRequestEvent>())).thenThrow(FunctionalityNotAvailable())

        assertThrows<FunctionalityNotAvailable> { sut.handleRequest(anyApiGatewayProxyRequestEvent(), context) }

        verifyNoInteractions(service)
    }

    private fun anyApiGatewayProxyRequestEvent(
        request: CreatePaymentRequest = anyCreatePaymentRequest()
    ): APIGatewayProxyRequestEvent = apiGatewayEventRequest(
        method = HttpMethod.POST,
        path = PATH,
        authorization = ACCESS_TOKEN,
        body = """
            {"supplierOrderId":"${request.supplierOrderId}","amount":"${request.amount}","totalAmount":"${request.totalAmount}"}
        """.trimIndent()
    )

}