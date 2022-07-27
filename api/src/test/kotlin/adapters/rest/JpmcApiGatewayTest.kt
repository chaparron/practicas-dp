package adapters.rest

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import com.amazonaws.services.lambda.runtime.tests.annotations.Events
import com.amazonaws.services.lambda.runtime.tests.annotations.HandlerParams
import com.amazonaws.services.lambda.runtime.tests.annotations.Responses
import configuration.TestConfiguration
import domain.model.SaleInformation
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.JsonElement
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import org.slf4j.LoggerFactory
import wabi.rest2lambda.APPLICATION_JSON
import wabi.rest2lambda.CONTENT_TYPE

@ExtendWith(MockitoExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JpmcApiGatewayTest {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Mock
    private lateinit var context: Context

    private val configuration = TestConfiguration.mockedInstance()
    private val handler = JpmcApiGateway(configuration)

    @ParameterizedTest
    @HandlerParams(
        events = Events(folder = "aws/rest/commons/events", type = APIGatewayProxyRequestEvent::class),
        responses = Responses(folder = "aws/rest/commons/responses", type = APIGatewayProxyResponseEvent::class)
    )
    fun commonsEventsTest(event: APIGatewayProxyRequestEvent, expectedResponse: APIGatewayProxyResponseEvent) {
        givenSecurityCheckStubs(expectedResponse)

        val response = handler.handleRequest(event, context)

        assertResponse(
            actualResponse = response,
            expectedResponse = expectedResponse,
            ignoreBodyFields = arrayOf()
        )
    }

    @ParameterizedTest
    @HandlerParams(
        events = Events(folder = "aws/rest/saleInformation/events", type = APIGatewayProxyRequestEvent::class),
        responses = Responses(folder = "aws/rest/saleInformation/responses", type = APIGatewayProxyResponseEvent::class)
    )
    fun saleInformationEventsTest(event: APIGatewayProxyRequestEvent, expectedResponse: APIGatewayProxyResponseEvent) {
        givenSecurityCheckStubs(expectedResponse)
        whenExpectedStatusBalanceResponseContext(
            expectedResponse.statusCode,
            event.queryStringParameters["amount"]
        )

        val response = handler.handleRequest(event, context)
        assertResponse(
            actualResponse = response,
            expectedResponse = expectedResponse,
            ignoreBodyFields = arrayOf("id", "created", "invoice.id", "loan.id", "loan.created")
        )
    }

    private fun whenExpectedStatusBalanceResponseContext(statusCode: Int, customerIdParam: String?) {
        val saleInformationService = configuration.saleInformationService
        logger.trace("Stubbing response $statusCode-$customerIdParam")
        when (statusCode) {
            200 -> doReturn(
                SaleInformation(
                    bankId = "",
                    merchantId = "",
                    terminalId = "",
                    encData = ""
                )
            ).whenever(saleInformationService).getSaleInformation(any())
        }
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
            Assertions.assertThat(actualResponseBody)
                .usingRecursiveComparison()
                .ignoringFields(*ignoreBodyFields)
                .isEqualTo(expectedResponseBody)
        }
    }

    private fun givenSecurityCheckStubs(expectedResponse: APIGatewayProxyResponseEvent) {
        val authorizerWrapper = configuration.authorizerWrapper
        val stateValidationEnabled = configuration.jpmcStateValidationConfig

        whenever(stateValidationEnabled.enabled).doReturn(true)
        whenever(stateValidationEnabled.availableFor).doReturn(mutableListOf("IN-MH"))

        if (expectedResponse.body != null) {
            when (expectedResponse.statusCode) {
                200, 404 -> {
                    whenever(authorizerWrapper.requestedByUser(any())).doReturn(true)
                    whenever(authorizerWrapper.matchAnyAuthority(any())).doReturn(true)
                    whenever(authorizerWrapper.getState()).doReturn("IN-MH")
                }
                403 -> whenever(authorizerWrapper.requestedByUser(any())).doReturn(false)
                else -> {
                }
            }
        }
    }

}
