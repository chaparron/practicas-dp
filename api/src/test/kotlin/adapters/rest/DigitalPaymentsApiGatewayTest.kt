package adapters.rest

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import com.amazonaws.services.lambda.runtime.tests.annotations.Events
import com.amazonaws.services.lambda.runtime.tests.annotations.HandlerParams
import com.amazonaws.services.lambda.runtime.tests.annotations.Responses
import configuration.MainConfiguration.jsonMapper
import configuration.TestConfiguration
import domain.model.CreatePaymentResponse
import domain.model.PaymentForUpdate
import domain.model.PaymentStatus
import domain.model.UpdatePaymentResponse
import domain.model.errors.FunctionalityNotAvailable
import domain.model.errors.UpdatePaymentException
import domain.services.providers.Provider
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.JsonElement
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.whenever
import org.slf4j.LoggerFactory
import wabi.rest2lambda.APPLICATION_JSON
import wabi.rest2lambda.CONTENT_TYPE
import java.math.BigDecimal


@ExtendWith(MockitoExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DigitalPaymentsApiGatewayTest {

    @Mock
    private lateinit var context: Context

    private val configuration = TestConfiguration.mockedInstance()
    private val stateValidatorService = configuration.stateValidatorService
    private val createPaymentService = configuration.createPaymentService
    private val paymentProviderService = configuration.paymentProviderService
    private val updatePaymentService = configuration.updatePaymentService
    private var handler: DigitalPaymentsApiGateway = DigitalPaymentsApiGateway(configuration)

    companion object {
        private val logger = LoggerFactory.getLogger(DigitalPaymentsApiGatewayTest::class.java)
        private const val STATE = "IN-MH"
        private const val UNKNOWN_STATE = "UNK_STATE"
    }

    @BeforeEach
    fun setup() {
        Mockito.reset(context, stateValidatorService)
    }

    @Nested
    inner class GetCreatePaymentResponse {
        @ParameterizedTest
        @HandlerParams(
            events = Events(folder = "aws/rest/commons/events", type = APIGatewayProxyRequestEvent::class),
            responses = Responses(folder = "aws/rest/commons/responses", type = APIGatewayProxyResponseEvent::class)
        )
        fun commonsEventsTest(event: APIGatewayProxyRequestEvent, expectedResponse: APIGatewayProxyResponseEvent) {
            givenSecurityCheckStubs(expectedResponse)

            val response = handler.handleRequest(event, context)

            assertResponse(
                actualResponse = response, expectedResponse = expectedResponse, ignoreBodyFields = arrayOf()
            )
        }

        @ParameterizedTest
        @HandlerParams(
            events = Events(folder = "aws/rest/createPayment/events", type = APIGatewayProxyRequestEvent::class),
            responses = Responses(
                folder = "aws/rest/createPayment/responses", type = APIGatewayProxyResponseEvent::class
            )
        )
        fun createPaymentEventsTest(
            event: APIGatewayProxyRequestEvent, expectedResponse: APIGatewayProxyResponseEvent
        ) {
            givenSecurityCheckStubs(expectedResponse)
            whenExpectedCreatePaymentResponseContext(
                expectedResponse.statusCode, event.body
            )

            val response = handler.handleRequest(event, context)
            assertResponse(
                actualResponse = response,
                expectedResponse = expectedResponse,
            )
        }

        private fun whenExpectedCreatePaymentResponseContext(statusCode: Int, body: String) {
            logger.trace("Stubbing response $statusCode-$body")
            when (statusCode) {
                200 -> doReturn(createPaymentResponse()).whenever(createPaymentService)
                    .createPayment(jsonMapper.decodeFromString(body))
                400 -> doThrow(FunctionalityNotAvailable(UNKNOWN_STATE)).whenever(stateValidatorService)
                    .validate(any<APIGatewayProxyRequestEvent>())
            }
        }

        private fun createPaymentResponse() =
            CreatePaymentResponse(bankId = "", merchantId = "", terminalId = "", encData = "")
    }

    @Nested
    inner class PaymentProviders {

        @ParameterizedTest
        @HandlerParams(
            events = Events(folder = "aws/rest/paymentProviders/events", type = APIGatewayProxyRequestEvent::class),
            responses = Responses(
                folder = "aws/rest/paymentProviders/responses", type = APIGatewayProxyResponseEvent::class
            )
        )
        fun paymentProvidersEventsTest(
            event: APIGatewayProxyRequestEvent, expectedResponse: APIGatewayProxyResponseEvent
        ) {
            givenSecurityCheckStubs(expectedResponse)
            whenExpectedProvidersResponseContext(
                expectedResponse.statusCode, event.queryStringParameters["supplierId"]?.toLong(), STATE
            )

            val response = handler.handleRequest(event, context)
            assertResponse(
                actualResponse = response,
                expectedResponse = expectedResponse,
            )
        }

        private fun whenExpectedProvidersResponseContext(statusCode: Int, supplierId: Long?, state: String) {
            whenever(stateValidatorService.getState(any())).thenReturn(STATE)

            logger.trace("Stubbing response $statusCode-$supplierId-$state")
            when (statusCode) {
                200 -> doReturn(listOf(Provider.JP_MORGAN)).whenever(paymentProviderService)
                    .availableProviders(supplierId!!)
            }
        }
    }

    @Nested
    inner class UpdatePayment {
        @ParameterizedTest
        @HandlerParams(
            events = Events(folder = "aws/rest/updatePayment/events", type = APIGatewayProxyRequestEvent::class),
            responses = Responses(
                folder = "aws/rest/updatePayment/responses", type = APIGatewayProxyResponseEvent::class
            )
        )
        fun updatePaymentEventsTest(
            event: APIGatewayProxyRequestEvent, expectedResponse: APIGatewayProxyResponseEvent
        ) {
            givenSecurityCheckStubs(expectedResponse)
            whenExpectedUpdatePaymentResponseContext(
                expectedResponse.statusCode, event.body
            )

            val response = handler.handleRequest(event, context)
            assertResponse(
                actualResponse = response,
                expectedResponse = expectedResponse,
            )
        }

        private fun whenExpectedUpdatePaymentResponseContext(statusCode: Int, body: String) {
            whenever(stateValidatorService.getState(any())).thenReturn(STATE)

            logger.trace("Stubbing response $statusCode-$body")
            when (statusCode) {
                200 -> doReturn(createUpdatePaymentResponse()).whenever(updatePaymentService)
                    .update(jsonMapper.decodeFromString(body))
                400 -> doThrow(UpdatePaymentException(createPaymentForUpdate())).whenever(updatePaymentService)
                    .update(any())
            }
        }

        private fun createPaymentForUpdate(): PaymentForUpdate = PaymentForUpdate(
            paymentId = 1,
            paymentOption = "PO",
            responseCode = "00",
            message = "MSG",
            encData = "ENC_DATA",
            status = PaymentStatus.PAID,
            lastUpdatedAt = "2022-09-02T10:18:38"
        )

        private fun createUpdatePaymentResponse() = UpdatePaymentResponse(
            paymentId = 1,
            supplierOrderId = 666,
            amount = BigDecimal.ONE,
            responseCode = "00",
            message = "OK"
        )
    }


    private fun assertResponse(
        actualResponse: APIGatewayProxyResponseEvent,
        expectedResponse: APIGatewayProxyResponseEvent,
        ignoreBodyFields: Array<String> = emptyArray()
    ) {
        Assertions.assertThat(actualResponse.statusCode).isEqualTo(expectedResponse.statusCode)

        if (actualResponse.headers?.get(CONTENT_TYPE) == APPLICATION_JSON) {
            val expectedResponseBody = configuration.jsonMapper.decodeFromString<JsonElement>(expectedResponse.body)
            val actualResponseBody = configuration.jsonMapper.decodeFromString<JsonElement>(actualResponse.body)
            Assertions.assertThat(actualResponseBody).usingRecursiveComparison().ignoringFields(*ignoreBodyFields)
                .isEqualTo(expectedResponseBody)
        }
    }

    private fun givenSecurityCheckStubs(expectedResponse: APIGatewayProxyResponseEvent) {
        val authorizerWrapper = configuration.authorizerWrapper

        if (expectedResponse.body != null) {
            when (expectedResponse.statusCode) {
                200, 404, 400 -> {
                    whenever(authorizerWrapper.requestedByUser(any())).doReturn(true)
                    whenever(authorizerWrapper.matchAnyAuthority(any())).doReturn(true)
                }
                403 -> whenever(authorizerWrapper.requestedByUser(any())).doReturn(false)
                else -> {
                    throw RuntimeException("statusCode ${expectedResponse.statusCode} not available")
                }
            }
        }
    }

}
