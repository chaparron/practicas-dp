package adapters.rest.handler

import apiGatewayEventRequest
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import domain.services.DefaultSupplierOrderDelayService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpMethod


@ExtendWith(MockitoExtension::class)
internal class SupplierOrderDelayHandlerTest {

    private var service: DefaultSupplierOrderDelayService = mock()
    private var context: Context = mock()

    private lateinit var sut: SupplierOrderDelayHandler

    companion object {
        private const val PATH = "/dp/supplierOrderDelay"
        private const val ACCESS_TOKEN = "my-token"
        private const val SUPPLIER_ORDER_ID_PARAM = "supplierOrderId"
        private const val SUPPLIER_ORDER_ID_VALUE = "666"
    }

    @BeforeEach
    fun setUp() {
        sut = SupplierOrderDelayHandler(service)
    }


    @Test
    fun `should return true on delayed orderId`() {
        // Given
        val supplierOrderId = SUPPLIER_ORDER_ID_VALUE.toLong()
        val event = anyApiGatewayProxyRequestEvent()
        val expected = "true"

        whenever(service.isDelayed(supplierOrderId)).thenReturn(true)
        // When
        val response = sut.handleRequest(event, context).body
        // Then
        assertEquals(expected, response)

        verify(service).isDelayed(supplierOrderId)

    }
    @Test
    fun `should return false on in time orderId`() {
        // Given
        val supplierOrderId = SUPPLIER_ORDER_ID_VALUE.toLong()
        val event = anyApiGatewayProxyRequestEvent()
        val expected = "false"

        whenever(service.isDelayed(supplierOrderId)).thenReturn(false)
        // When
        val response = sut.handleRequest(event, context).body
        // Then
        assertEquals(expected, response)

        verify(service).isDelayed(supplierOrderId)

    }

    private fun anyApiGatewayProxyRequestEvent(): APIGatewayProxyRequestEvent =
        apiGatewayEventRequest(
            method = HttpMethod.GET,
            path = PATH,
            authorization = ACCESS_TOKEN,
            queryParams = mapOf(SUPPLIER_ORDER_ID_PARAM to SUPPLIER_ORDER_ID_VALUE)
        )
}
