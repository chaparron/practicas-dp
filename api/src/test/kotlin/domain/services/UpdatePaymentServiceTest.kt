package domain.services

import adapters.repositories.jpmc.JpmcPaymentRepository
import com.wabi2b.jpmc.sdk.usecase.sale.EncData
import com.wabi2b.jpmc.sdk.usecase.sale.PaymentData
import com.wabi2b.jpmc.sdk.usecase.sale.PaymentService
import com.wabi2b.jpmc.sdk.usecase.sale.PaymentStatus
import domain.model.JpmcPaymentInformation
import domain.model.PaymentForUpdate
import domain.model.UpdatePaymentResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import randomBigDecimal
import randomString
import toPaymentMethod
import wabi2b.payment.async.notification.sdk.WabiPaymentAsyncNotificationSdk
import wabi2b.payments.common.model.dto.PaymentType
import wabi2b.payments.common.model.dto.PaymentUpdated
import wabi2b.payments.common.model.dto.type.PaymentResult
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

@ExtendWith(MockitoExtension::class)
class UpdatePaymentServiceTest {

    @Mock
    private lateinit var paymentService: PaymentService

    @Mock
    private lateinit var repository: JpmcPaymentRepository

    @Mock
    private lateinit var wabiPaymentAsyncNotificationSdk: WabiPaymentAsyncNotificationSdk

    @Captor
    lateinit var paymentCaptor: ArgumentCaptor<PaymentForUpdate>

    @InjectMocks
    private lateinit var sut: UpdatePaymentService

    companion object {
        private const val SUCCESS_RESPONSE_CODE = "00"
    }

    @Test
    fun `given a valid encData when update then save information in database`() {
        //Given
        val encData = anyEncData(SUCCESS_RESPONSE_CODE)
        val paymentUpdated = PaymentUpdated(
            supplierOrderId = encData.supplierOrderId.toLong(),
            paymentType = PaymentType.DIGITAL_PAYMENT,
            paymentId = encData.txnRefNo.toLong(),
            resultType = if (encData.responseCode == SUCCESS_RESPONSE_CODE) PaymentResult.SUCCESS else PaymentResult.FAILED,
            amount = encData.amount.toBigDecimal(),
            paymentMethod = encData.paymentOption.toPaymentMethod()
        )

        val expectedResponse = UpdatePaymentResponse(
            paymentId = encData.txnRefNo.toLong(),
            supplierOrderId = encData.supplierOrderId.toLong(),
            amount = encData.amount.toBigDecimal(),
            responseCode = encData.responseCode,
            message = encData.message
        )

        val paymentData = PaymentData(
            paymentId = encData.txnRefNo.toLong(),
            supplierOrderId = encData.supplierOrderId.toLong(),
            amount = encData.amount.toBigDecimal(),
            paymentOption = "dc",
            responseCode = encData.responseCode,
            message = encData.message,
            encData = encData.toString(),
            status = PaymentStatus.PAID,
            paymentType = PaymentType.DIGITAL_PAYMENT,
            paymentMethod = encData.paymentOption.toPaymentMethod(),
            resultType = PaymentResult.SUCCESS
        )

        whenever(paymentService.createPaymentData(any())).thenReturn(paymentData)

        //When
        val response = sut.update(anyPaymentInformation())

        //Then
        assertEquals(expectedResponse, response)

        // Check some fields with captor
        verify(repository).update(capture(paymentCaptor))
        val payment = paymentCaptor.value
        assertNotNull(paymentUpdated)
        assertEquals(PaymentStatus.PAID, payment.status)

        verify(paymentService).createPaymentData(any())
        verify(wabiPaymentAsyncNotificationSdk).notify(paymentUpdated)
    }

    @Test
    fun `Given an encData with invalid payment method should throw InvalidPaymentMethod exception`() {
        //Given
        val encData = anyEncData(SUCCESS_RESPONSE_CODE, paymentMethod = randomString())

        whenever(paymentService.createPaymentData(any())).doThrow(InvalidPaymentMethodException(encData.paymentOption))

        //When
        assertFailsWith<InvalidPaymentMethodException> {
            sut.update(anyPaymentInformation())
        }

        //Then
        verifyNoInteractions(wabiPaymentAsyncNotificationSdk)
        verifyNoInteractions(repository)

    }

    private fun anyPaymentInformation() = JpmcPaymentInformation(
        encData = randomString()
    )

    private fun anyEncData(responseCode: String, paymentMethod: String = "cc") = EncData(
        txnRefNo = Random.nextLong().toString(),
        merchantId = randomString(),
        amount = randomBigDecimal().toString(),
        terminalId = randomString(),
        responseCode = responseCode,
        message = randomString(),
        mcc = randomString(),
        cardNum = randomString(),
        retRefNo = randomString(),
        authCode = randomString(),
        ucap = randomString(),
        cavv = randomString(),
        enrolled = randomString(),
        authStatus = randomString(),
        bankId = randomString(),
        currency = randomString(),
        paymentOption = paymentMethod,
        secureHash = randomString(),
        supplierOrderId = Random.nextLong().toString()
    )
}
