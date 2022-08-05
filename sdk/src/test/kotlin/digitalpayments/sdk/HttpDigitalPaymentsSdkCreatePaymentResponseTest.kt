package digitalpayments.sdk

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import digitalpayments.sdk.builders.apiResponse.ErrorResponseBuilder.buildApiRequestErrorResponse
import digitalpayments.sdk.configuration.SdkConfiguration
import digitalpayments.sdk.model.CreatePaymentRequest
import digitalpayments.sdk.model.CreatePaymentResponse
import domain.model.errors.JpmcErrorReason
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.serialization.encodeToString
import org.junit.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import reactor.test.StepVerifier
import reactor.test.verifyError
import wabi.sdk.AccessDenied
import wabi.sdk.Forbidden
import wabi.sdk.GenericSdkError
import java.net.URI

class HttpDigitalPaymentsSdkCreatePaymentResponseTest : AbstractSdkTest() {
    companion object {
        private const val PATH = "/dp/jpmc/createPayment"
        private const val AMOUNT_KEY = "amount"
        private const val ACCESS_TOKEN = "dummy.jwt.token"
    }

    private val root = URI.create("http://localhost:${port()}")
    private val digitalPaymentsSdk: DigitalPaymentsSdk = HttpDigitalPaymentsSdk(root)
    private val mapper = SdkConfiguration.jsonMapper

    @Test
    fun `given bad request when createPayment then throw access denied with statusCode is 401`() {
        stubFor(
            any(urlPathEqualTo(PATH)).willReturn(aResponse().withStatus(HttpStatus.UNAUTHORIZED.value()))
        )

        val request = CreatePaymentRequest(
            supplierOrderId = "1234",
            amount = "100",
            totalAmount = "300"
        )

        StepVerifier
            .create(digitalPaymentsSdk.createPayment(request, ACCESS_TOKEN))
            .verifyError(AccessDenied::class)
    }

    @Test
    fun `given bad request when createPayment then throw forbidden with statusCode is 403`() {
        stubFor(
            any(urlPathEqualTo(PATH)).willReturn(aResponse().withStatus(HttpStatus.FORBIDDEN.value()))
        )

        val request = CreatePaymentRequest(
            supplierOrderId = "1234",
            amount = "100",
            totalAmount = "300"
        )

        StepVerifier
            .create(digitalPaymentsSdk.createPayment(request, ACCESS_TOKEN))
            .verifyError(Forbidden::class)
    }

    @Test
    fun `given bad request when createPayment then throw BadRequest with statusCode is 400`() {

        val response = buildApiRequestErrorResponse(JpmcErrorReason.MISSING_AMOUNT, AMOUNT_KEY)

        stubFor(
            any(urlPathEqualTo(PATH))
                .withHeader(HttpHeaders.CONTENT_TYPE, EqualToPattern(MediaType.APPLICATION_JSON_VALUE))
                .withHeader(HttpHeaders.AUTHORIZATION, EqualToPattern("Bearer $ACCESS_TOKEN"))
                .willReturn(
                    aResponse()
                        .withBody(mapper.encodeToString(response))
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                )
        )

        val request = CreatePaymentRequest(
            supplierOrderId = "1234",
            amount = "100",
            totalAmount = "300"
        )

        StepVerifier
            .create(digitalPaymentsSdk.createPayment(request, ACCESS_TOKEN))
            .verifyErrorSatisfies {
                assertTrue(it is GenericSdkError)
                val ex = it as GenericSdkError
                with(response.errors.first()) {
                    assertEquals(entity, ex.entity)
                    assertEquals(property, ex.property)
                    assertEquals(invalidValue, ex.invalidValue)
                    assertEquals(message, ex.errorMessage)
                }
            }
    }

    @Test
    fun `given valid request when createPayment then return success saleInformationResponse`() {

        val response = CreatePaymentResponse(bankId = "", merchantId = "", terminalId = "", encData = "")

        stubFor(
            any(urlPathEqualTo(PATH))
                .withHeader(HttpHeaders.CONTENT_TYPE, EqualToPattern(MediaType.APPLICATION_JSON_VALUE))
                .withHeader(HttpHeaders.AUTHORIZATION, EqualToPattern("Bearer $ACCESS_TOKEN"))
                .willReturn(ok(mapper.encodeToString(response)))
        )

        val request = CreatePaymentRequest(
            supplierOrderId = "1234",
            amount = "100",
            totalAmount = "300"
        )

        StepVerifier
            .create(digitalPaymentsSdk.createPayment(request, ACCESS_TOKEN))
            .expectNext(response)
            .verifyComplete()

    }

}