package adapters.rest.handler

import apiGatewayEventRequest
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.wabi2b.serializers.BigDecimalSerializer
import com.wabi2b.serializers.InstantSerializer
import com.wabi2b.serializers.URISerializer
import com.wabi2b.serializers.UUIDStringSerializer
import domain.model.JpmcPaymentInformation
import domain.model.UpdatePaymentResponse
import domain.services.UpdatePaymentService
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
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
import randomBigDecimal
import randomLong
import randomString

@ExtendWith(MockitoExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JpmcUpdatePaymentHandlerTest {

    private var context: Context = mock()
    private var service: UpdatePaymentService = mock()
    private var jsonMapper: Json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        serializersModule = SerializersModule {
            contextual(InstantSerializer)
            contextual(UUIDStringSerializer)
            contextual(URISerializer)
            contextual(BigDecimalSerializer)
        }
    }

    private lateinit var sut: JpmcUpdatePaymentHandler

    companion object {
        private const val PATH = "/dp/jpmc/updatePayment"
        private const val ACCESS_TOKEN = "my-token"
        private const val RESPONSE_CODE = "X"
        private const val MESSAGE = "X"
    }

    @BeforeEach
    fun setUp() {
        sut = JpmcUpdatePaymentHandler(service, jsonMapper, updatePaymentDummyEnabled = false)
    }

    @Test
    fun `given valid encData when updatePayment then return valid response `() {
        val request = anyJpmcPaymentInformation()
        val somePaymentId = randomLong()
        val someSupplierOrderId = randomLong()
        val amount = randomBigDecimal()
        val expected = "{\"paymentId\":${somePaymentId},\"supplierOrderId\":${someSupplierOrderId},\"amount\":\"${amount}\",\"responseCode\":\"${RESPONSE_CODE}\",\"message\":\"${MESSAGE}\"}"

        whenever(service.update(request))
            .thenReturn(
                UpdatePaymentResponse(
                    somePaymentId,
                    someSupplierOrderId,
                    amount,
                    RESPONSE_CODE,
                    MESSAGE
                )
            )

        val response = sut.handleRequest(anyApiGatewayProxyRequestEvent(request), context)

        assertEquals(expected, response.body)

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
