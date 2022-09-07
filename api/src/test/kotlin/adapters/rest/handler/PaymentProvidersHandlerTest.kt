package adapters.rest.handler

import apiGatewayEventRequest
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.wabi2b.serializers.BigDecimalToFloatSerializer
import com.wabi2b.serializers.InstantSerializer
import com.wabi2b.serializers.URISerializer
import com.wabi2b.serializers.UUIDStringSerializer
import domain.services.providers.PaymentProviderService
import domain.services.providers.Provider
import domain.services.state.StateValidatorService
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.http.HttpMethod
import randomString
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PaymentProvidersHandlerTest {

    private var context: Context = mock()
    private var service: PaymentProviderService = mock()
    private var stateValidatorService: StateValidatorService = mock()

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

    private lateinit var sut: PaymentProvidersHandler

    companion object {
        private const val PATH = "/dp/paymentProviders"
        private const val ACCESS_TOKEN = "my-token"
        private const val SUPPLIER_ID_PARAM = "supplierId"
        private const val SUPPLIER_ID_VALUE = "666"
        private const val EMPTY_LIST_BODY = "[]"
    }

    @BeforeEach
    fun setUp() {
        sut = PaymentProvidersHandler(service, stateValidatorService, jsonMapper)
    }

    @Test
    fun `given state validation ok when handle paymentProviders then return valid response `() {
        val event = anyApiGatewayProxyRequestEvent()
        val state = randomString()
        val supplierId = SUPPLIER_ID_VALUE.toLong()
        val providers = listOf(Provider.JP_MORGAN)
        val expected = jsonMapper.encodeToString(providers)

        whenever(stateValidatorService.getState(event)).thenReturn(state)
        whenever(stateValidatorService.validate(state)).thenReturn(true)
        whenever(service.availableProviders(supplierId)).thenReturn(providers)

        val response = sut.handleRequest(event, context)
        assertEquals(expected, response.body)

        verify(stateValidatorService).getState(event)
        verify(stateValidatorService).validate(state)
        verify(service).availableProviders(supplierId)
    }

    @Test
    fun `given state validation ko when handle paymentProviders then return empty list`() {
        val event = anyApiGatewayProxyRequestEvent()
        val state = randomString()

        whenever(stateValidatorService.getState(event)).thenReturn(state)
        whenever(stateValidatorService.validate(state)).thenReturn(false)

        val response = sut.handleRequest(event, context)
        assertEquals(EMPTY_LIST_BODY, response.body)

        verify(stateValidatorService).getState(event)
        verify(stateValidatorService).validate(state)
        verifyNoInteractions(service)
    }

    private fun anyApiGatewayProxyRequestEvent(): APIGatewayProxyRequestEvent =
        apiGatewayEventRequest(
            method = HttpMethod.GET,
            path = PATH,
            authorization = ACCESS_TOKEN,
            queryParams = mapOf(SUPPLIER_ID_PARAM to SUPPLIER_ID_VALUE)
        )

}
