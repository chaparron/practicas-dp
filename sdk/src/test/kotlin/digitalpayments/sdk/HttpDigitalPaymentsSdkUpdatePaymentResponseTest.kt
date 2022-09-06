package digitalpayments.sdk

import ACCESS_TOKEN
import CLIENT_TOKEN_PREFIX
import CUSTOM_EXCEPTION_HEADER
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import digitalpayments.sdk.configuration.SdkConfiguration
import digitalpayments.sdk.model.UpdatePaymentRequest
import digitalpayments.sdk.model.UpdatePaymentResponse
import domain.model.exceptions.DigitalPaymentsDetailedError
import domain.model.exceptions.ErrorReason
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.serialization.encodeToString
import org.junit.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import randomBigDecimal
import randomLong
import randomString
import reactor.test.StepVerifier
import wabi.sdk.impl.CustomSdkException
import java.net.URI

class HttpDigitalPaymentsSdkUpdatePaymentResponseTest : AbstractSdkTest() {
    companion object {
        private const val PATH = "/dp/jpmc/updatePayment"
    }

    private val root = URI.create("http://localhost:${port()}")
    private val digitalPaymentsSdk: DigitalPaymentsSdk = HttpDigitalPaymentsSdk(root)
    private val mapper = SdkConfiguration.jsonMapper

    @Test
    fun `given valid request when updatePayment then return success updatePaymentResponse`() {

        val response = UpdatePaymentResponse(
            paymentId = randomLong(),
            supplierOrderId = randomLong(),
            amount = randomBigDecimal(),
            responseCode = "",
            message = ""
        )

        stubFor(
            any(urlPathEqualTo(PATH))
                .withHeader(HttpHeaders.CONTENT_TYPE, EqualToPattern(MediaType.APPLICATION_JSON_VALUE))
                .withHeader(HttpHeaders.AUTHORIZATION, EqualToPattern("$CLIENT_TOKEN_PREFIX $ACCESS_TOKEN"))
                .willReturn(ok(mapper.encodeToString(response)))
        )

        val request = UpdatePaymentRequest(
            encData = "xxxx"
        )

        StepVerifier
            .create(digitalPaymentsSdk.updatePayment(request, ACCESS_TOKEN))
            .expectNext(response)
            .verifyComplete()

    }

    @Test
    fun `given FunctionalityNotAvailable when updatePayment then throw BadRequest with statusCode is 400`() {
        val reason = ErrorReason.FUNCTIONALITY_NOT_AVAILABLE
        val detailError = "The current functionality is not available for IN-UNK"
        val response = DigitalPaymentsDetailedError(reason, detailError)

        checkErrors(response, reason, detailError)
    }

    @Test
    fun `given ClientTokenException when updatePayment then throw BadRequest with statusCode is 400`() {
        val reason = ErrorReason.CLIENT_TOKEN_EXCEPTION
        val detailError = "An error occur trying to retrieve token for client user XX"
        val response = DigitalPaymentsDetailedError(reason, detailError)

        checkErrors(response, reason, detailError)
    }

    @Test
    fun `given UpdatePaymentException when updatePayment then throw BadRequest with statusCode is 400`() {
        val reason = ErrorReason.UPDATE_PAYMENT_EXCEPTION
        val detailError = "There was an error updating the following payment: XXX"
        val response = DigitalPaymentsDetailedError(reason, detailError)

        checkErrors(response, reason, detailError)
    }

    @Test
    fun `given PaymentNotFound when updatePayment then throw BadRequest with statusCode is 400`() {
        val reason = ErrorReason.PAYMENT_NOT_FOUND_EXCEPTION
        val detailError = "Cannot find any jpmc information for XXX"
        val response = DigitalPaymentsDetailedError(reason, detailError)

        checkErrors(response, reason, detailError)
    }

    @Test
    fun `given TotalAmountReached when updatePayment then throw BadRequest with statusCode is 400`() {
        val reason = ErrorReason.TOTAL_AMOUNT_REACHED
        val detailError = "Total amount reached"
        val response = DigitalPaymentsDetailedError(reason, detailError)

        checkErrors(response, reason, detailError)
    }

    @Test
    fun `given unexpected error when updatePayment then throw InternalServerError with statusCode is 500`() {
        val reason = ErrorReason.UNKNOWN
        val detailError = ErrorReason.UNKNOWN.detail()
        val response = DigitalPaymentsDetailedError(reason, detailError)

        checkErrors(response, reason, detailError, HttpStatus.INTERNAL_SERVER_ERROR.value())
    }

    private fun checkErrors(
        response: DigitalPaymentsDetailedError,
        reason: ErrorReason,
        detailError: String,
        httpStatusCode: Int = HttpStatus.BAD_REQUEST.value()
    ) {
        stubFor(
            any(urlPathEqualTo(PATH))
                .withHeader(HttpHeaders.CONTENT_TYPE, EqualToPattern(MediaType.APPLICATION_JSON_VALUE))
                .withHeader(HttpHeaders.AUTHORIZATION, EqualToPattern("$CLIENT_TOKEN_PREFIX $ACCESS_TOKEN"))
                .willReturn(
                    aResponse()
                        .withBody(mapper.encodeToString(response))
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withHeader(CUSTOM_EXCEPTION_HEADER, "true")
                        .withStatus(httpStatusCode)
                )
        )

        val request = UpdatePaymentRequest(
            encData = randomString()
        )

        StepVerifier
            .create(digitalPaymentsSdk.updatePayment(request, ACCESS_TOKEN))
            .verifyErrorSatisfies {
                assertTrue(it is CustomSdkException)

                val ex = it as CustomSdkException
                assertEquals(reason.name, ex.error.reason)
                assertEquals(detailError, ex.error.detail)
            }
    }

}
