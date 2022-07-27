package adapters.rest.handler

import adapters.rest.validations.Security
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import configuration.EnvironmentVariable
import domain.model.SaleInformation
import domain.model.errors.FunctionalityNotAvailable
import domain.services.SaleInformationService
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
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
    private var service: SaleInformationService = mock()
    private var jsonMapper: Json = Json { ignoreUnknownKeys = true }
    private var stateValidationConfig: EnvironmentVariable.JpmcStateValidationConfig = mock()
    private var security: Security = mock()

    private lateinit var sut: JpmcSaleInformationHandler

    companion object {
        private const val PATH = "/jpmc/saleInformation"
        private const val ACCESS_TOKEN = "my-token"
        private const val ACCEPTED_STATE = "IN-MH"
        private const val UNKNOWN_STATE = "XX-XX"

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
        Mockito.reset(service, stateValidationConfig, security)
        sut = JpmcSaleInformationHandler(service, jsonMapper, stateValidationConfig, security)
    }

    @Test
    fun `given state validation disable when getSaleInformation then return valid information `() {
        whenever(stateValidationConfig.enabled).thenReturn(false)

        whenever(service.getSaleInformation(any()))
            .thenReturn(SaleInformation(BANK_ID, MERCHANT_ID, TERMINAL_ID, ENC_DATA))

        val response = sut.handleRequest(anyApiGatewayProxyRequestEvent(), context)

        assertEquals(EXPECTED_JSON_RESPONSE, response.body)

        verify(stateValidationConfig, times(1)).enabled
        verify(stateValidationConfig, never()).availableFor

        verifyNoInteractions(security)
        verify(service).getSaleInformation(any())
    }

    @Test
    fun `given state validation enabled and valid state when getSaleInformation then return valid information`() {
        whenever(stateValidationConfig.enabled).thenReturn(true)
        whenever(stateValidationConfig.availableFor).thenReturn(mutableListOf(ACCEPTED_STATE))

        val securityWraper = mock<Security.AuthorizerWrapper>()
        whenever(security.buildAuthorizer(any())).thenReturn(securityWraper)
        whenever(securityWraper.getState()).thenReturn(ACCEPTED_STATE)

        whenever(service.getSaleInformation(any()))
            .thenReturn(SaleInformation(BANK_ID, MERCHANT_ID, TERMINAL_ID, ENC_DATA))


        val response = sut.handleRequest(anyApiGatewayProxyRequestEvent(), context)

        assertEquals(EXPECTED_JSON_RESPONSE, response.body)

        verify(stateValidationConfig).enabled
        verify(stateValidationConfig).availableFor
        verify(security).buildAuthorizer(any())
        verify(service).getSaleInformation(any())

    }

    @Test
    fun `given state validation enabled and invalid state when getSaleInformation then throw `() {
        whenever(stateValidationConfig.enabled).thenReturn(true)
        whenever(stateValidationConfig.availableFor).thenReturn(mutableListOf(ACCEPTED_STATE))

        val securityWraper = mock<Security.AuthorizerWrapper>()
        whenever(security.buildAuthorizer(any())).thenReturn(securityWraper)
        whenever(securityWraper.getState()).thenReturn(UNKNOWN_STATE)

        assertThrows<FunctionalityNotAvailable> { sut.handleRequest(anyApiGatewayProxyRequestEvent(), context) }


        verify(stateValidationConfig).enabled
        verify(stateValidationConfig).availableFor
        verify(security).buildAuthorizer(any())

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

    private fun apiGatewayEventRequest(
        method: HttpMethod = HttpMethod.POST,
        path: String,
        authorization: String,
        queryParameters: Map<String, String> = emptyMap(),
    ): APIGatewayProxyRequestEvent = APIGatewayProxyRequestEvent()
        .withPath(path)
        .withHeaders(mapOf("Authorization" to authorization))
        .withQueryStringParameters(queryParameters)
        .withHttpMethod(method.name)

    private fun jacksonMapper(): ObjectMapper {
        return jacksonObjectMapper()
            .apply {
                this.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
                this.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
                this.configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
            }
    }
}
