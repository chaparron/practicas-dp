package adapters.rest.handler

import com.wabi2b.jpmc.sdk.usecase.sale.PaymentStatus
import com.wabi2b.serializers.BigDecimalSerializer
import com.wabi2b.serializers.InstantSerializer
import com.wabi2b.serializers.URISerializer
import com.wabi2b.serializers.UUIDStringSerializer
import domain.model.PaymentForUpdate
import domain.model.errors.*
import domain.model.exceptions.DigitalPaymentsDetailedError
import domain.model.exceptions.ErrorReason
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import randomLong
import randomString
import software.amazon.awssdk.http.HttpStatusCode
import wabi.sdk.impl.CustomSdkException
import java.time.Instant
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class ErrorHandlerTest {

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

    private lateinit var sut: ErrorHandler

    @BeforeEach
    fun setUp() {
        sut = ErrorHandler(jsonMapper)
    }

    @Test
    fun `given FunctionalityNotAvailable when handle exception then return expected response `() {
        // Exception to test
        val unknownState = "UNK-STATE"
        val exceptionToTest = FunctionalityNotAvailable(unknownState)

        // Properties for exception
        val reason = ErrorReason.FUNCTIONALITY_NOT_AVAILABLE
        val detail = "The current functionality is not available for $unknownState"

        // Test
        checkHandler(exceptionToTest, reason, detail)
    }

    @Test
    fun `given ClientTokenException when handle exception then return expected response `() {
        // Exception to test
        val clientUser = "CLIENT_USER"
        val exceptionToTest = ClientTokenException(clientUser)

        // Properties for exception
        val reason = ErrorReason.CLIENT_TOKEN_EXCEPTION
        val detail = "An error occur trying to retrieve token for client user $clientUser"

        // Test
        checkHandler(exceptionToTest, reason, detail)
    }

    @Test
    fun `given MissingFieldException when handle exception then return expected response `() {
        // Exception to test
        val field = "FIELD"
        val exceptionToTest = MissingFieldException(field)

        // Properties for exception
        val reason = ErrorReason.MISSING_FIELD_EXCEPTION
        val detail = "Missing required field: $field"

        // Test
        checkHandler(exceptionToTest, reason, detail)
    }

    @Test
    fun `given UpdatePaymentException when handle exception then return expected response `() {
        // Exception to test
        val payment = anyPaymentForUpdate()
        val exceptionToTest = UpdatePaymentException(payment)

        // Properties for exception
        val reason = ErrorReason.UPDATE_PAYMENT_EXCEPTION
        val detail = "There was an error updating the following payment: $payment"

        // Test
        checkHandler(exceptionToTest, reason, detail)
    }

    @Test
    fun `given PaymentNotFound when handle exception then return expected response `() {
        // Exception to test
        val jpmcId = "JPMC_ID"
        val exceptionToTest = PaymentNotFound(jpmcId)

        // Properties for exception
        val reason = ErrorReason.PAYMENT_NOT_FOUND_EXCEPTION
        val detail = "Cannot find any jpmc information for $jpmcId"

        // Test
        checkHandler(exceptionToTest, reason, detail)
    }

    @Test
    fun `given CustomSdkException when handle TotalAmountReached exception from sdk then return expected response `() {
        // Exception to test
        val exceptionToTest = CustomSdkException(
            wabi.sdk.impl.DetailedError(
                reason = "TOTAL_AMOUNT_REACHED",
                detail = "Total amount 10000 reached"
            )
        )

        // Properties for exception
        val reason = ErrorReason.TOTAL_AMOUNT_REACHED
        val detail = "Total amount 10000 reached"

        // Test
        checkHandler(exceptionToTest, reason, detail)
    }

    @Test
    fun `given unexpected error with message when handle exception then return expected custom InternalServerError response `() {

        // Exception to test
        val message = "InternalServerError"
        val exceptionToTest = RuntimeException(message)

        // Properties for exception
        val reason = ErrorReason.UNKNOWN

        // Test
        checkHandler(exceptionToTest, reason, message, HttpStatusCode.INTERNAL_SERVER_ERROR)
    }

    @Test
    fun `given unexpected error without message when handle exception then return expected custom InternalServerError response `() {

        // Exception to test
        val exceptionToTest = RuntimeException()

        // Properties for exception
        val reason = ErrorReason.UNKNOWN
        val detail = ErrorReason.UNKNOWN.detail()

        // Test
        checkHandler(exceptionToTest, reason, detail, HttpStatusCode.INTERNAL_SERVER_ERROR)
    }

        private fun checkHandler(
        exception: Throwable,
        reason: ErrorReason,
        detail: String,
        httpStatusCode: Int = HttpStatusCode.BAD_REQUEST
    ) {
        val handle = sut.handle(exception)

        val expected = jsonMapper.encodeToString(DigitalPaymentsDetailedError(reason, detail))
        assertAll("Check body and status",
            { assertEquals(expected, handle.body) },
            { assertEquals(httpStatusCode, handle.statusCode) }
        )
    }

    private fun anyPaymentForUpdate(): PaymentForUpdate = PaymentForUpdate(
        paymentId = randomLong(),
        paymentOption = randomString(),
        responseCode = randomString(),
        message = randomString(),
        encData = randomString(),
        status = PaymentStatus.PAID,
        lastUpdatedAt = Instant.now().toString()
    )

}
