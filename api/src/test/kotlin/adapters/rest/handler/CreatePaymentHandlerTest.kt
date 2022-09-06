package adapters.rest.handler

import anyCreatePaymentRequest
import apiGatewayEventRequest
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.wabi2b.serializers.BigDecimalToFloatSerializer
import com.wabi2b.serializers.InstantSerializer
import com.wabi2b.serializers.URISerializer
import com.wabi2b.serializers.UUIDStringSerializer
import domain.model.CreatePaymentRequest
import domain.model.CreatePaymentResponse
import domain.model.errors.FunctionalityNotAvailable
import domain.services.CreatePaymentService
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
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import randomString

@ExtendWith(MockitoExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CreatePaymentHandlerTest {

    private var context: Context = mock()
    private var service: CreatePaymentService = mock()
    private var jsonMapper: Json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        serializersModule = SerializersModule {
            contextual(InstantSerializer)
            contextual(UUIDStringSerializer)
            contextual(URISerializer)
            contextual(BigDecimalToFloatSerializer)
        }
    }
    private var stateValidatorService: StateValidatorService = mock()
    private var createPaymentDummyEnabled = false

    private lateinit var sut: CreatePaymentHandler

    companion object {
        private const val PATH = "/dp/jpmc/createPayment"
        private const val ACCESS_TOKEN = "my-token"

        private const val BANK_ID = "X"
        private const val MERCHANT_ID = "X"
        private const val TERMINAL_ID = "X"
        private const val ENC_DATA = "X"

        private const val EXPECTED_JSON_RESPONSE =
            "{\"bankId\":\"$BANK_ID\",\"merchantId\":\"$MERCHANT_ID\",\"terminalId\":\"$TERMINAL_ID\",\"encData\":\"$ENC_DATA\"}"
    }

    @BeforeEach
    fun setUp() {
        Mockito.reset(service, stateValidatorService)
        sut = CreatePaymentHandler(service, jsonMapper, stateValidatorService, createPaymentDummyEnabled)
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
        whenever(stateValidatorService.validate(any<APIGatewayProxyRequestEvent>()))
            .thenThrow(FunctionalityNotAvailable(randomString()))

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
            {"supplierOrderId":"${request.supplierOrderId}","amount":${request.amount},"invoiceId":"${request.invoiceId}"}
        """.trimIndent()
    )

}
