package adapters.rest.handler

import apiGatewayEventRequest
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import domain.model.SaleInformation
import domain.model.errors.FunctionalityNotAvailable
import domain.services.JpmcSaleInformationService
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
class JpmcSaleInformationHandlerTest {

    private var context: Context = mock()
    private var service: JpmcSaleInformationService = mock()
    private var jsonMapper: Json = Json { ignoreUnknownKeys = true }
    private var stateValidatorService: StateValidatorService = mock()

    private lateinit var sut: JpmcSaleInformationHandler

    companion object {
        private const val PATH = "/dp/jpmc/saleInformation"
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
        sut = JpmcSaleInformationHandler(service, jsonMapper, stateValidatorService)
    }

    @Test
    fun `given state validation ok when getSaleInformation then return valid information `() {
        whenever(service.getSaleInformation(any()))
            .thenReturn(SaleInformation(BANK_ID, MERCHANT_ID, TERMINAL_ID, ENC_DATA))

        val response = sut.handleRequest(anyApiGatewayProxyRequestEvent(), context)

        assertEquals(EXPECTED_JSON_RESPONSE, response.body)

        verify(stateValidatorService, times(1)).validate(any<APIGatewayProxyRequestEvent>())

        verify(service).getSaleInformation(any())
    }

    @Test
    fun `given state validation ko when getSaleInformation then throw custom exception FunctionalityNotAvailable`() {
        whenever(stateValidatorService.validate(any<APIGatewayProxyRequestEvent>())).thenThrow(FunctionalityNotAvailable())

        assertThrows<FunctionalityNotAvailable> { sut.handleRequest(anyApiGatewayProxyRequestEvent(), context) }

        verifyNoInteractions(service)
    }

    private fun anyApiGatewayProxyRequestEvent(): APIGatewayProxyRequestEvent = apiGatewayEventRequest(
        method = HttpMethod.DELETE,
        path = PATH,
        authorization = ACCESS_TOKEN,
        queryParameters = mapOf(
            AMOUNT_KEY to AMOUNT
        )
    )

}
