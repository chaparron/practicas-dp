package domain.services

import adapters.repositories.jpmc.JpmcPaymentRepository
import com.wabi2b.jpmc.sdk.security.cipher.aes.decrypt.AesDecrypterService
import com.wabi2b.jpmc.sdk.usecase.sale.EncData
import domain.model.JpmcPaymentInformation
import domain.model.Payment
import domain.model.PaymentStatus
import domain.model.UpdatePaymentResponse
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import randomString
import wabi2b.payment.async.notification.sdk.WabiPaymentAsyncNotificationSdk
import wabi2b.payments.common.model.dto.PaymentType
import wabi2b.payments.common.model.dto.PaymentUpdated
import wabi2b.payments.common.model.dto.type.PaymentResult
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import randomBigDecimal
import randomLong
import toPaymentMethod
import wabi2b.payments.common.model.dto.type.PaymentMethod

@ExtendWith(MockitoExtension::class)
class UpdatePaymentServiceTest {

    @Mock
    private lateinit var decrypter: AesDecrypterService

    @Mock
    private lateinit var jsonMapper: Json

    @Mock
    private lateinit var repository: JpmcPaymentRepository

    @Mock
    private lateinit var wabiPaymentAsyncNotificationSdk: WabiPaymentAsyncNotificationSdk

    @Captor
    lateinit var paymentCaptor: ArgumentCaptor<Payment>


    @InjectMocks
    private lateinit var sut: UpdatePaymentService

    companion object {
        private const val SUCCESS_RESPONSE_CODE = "00"
    }

    @Test
    fun `given a valid encData when update then save information in database`() {
        val encData = anyEncData(SUCCESS_RESPONSE_CODE)
        val paymentUpdated = PaymentUpdated(
            supplierOrderId = encData.supplierOrderId!!.toLong(),
            paymentType = PaymentType.DIGITAL_PAYMENT,
            paymentId = encData.txnRefNo.toLong(),
            resultType = if (encData.responseCode == SUCCESS_RESPONSE_CODE) PaymentResult.SUCCESS else PaymentResult.FAILED,
            amount = encData.amount.toBigDecimal(),
            paymentMethod = encData.paymentOption.toPaymentMethod()
        )

        val expectedResponse = UpdatePaymentResponse(
            paymentId = encData.txnRefNo,
            supplierOrderId = encData.supplierOrderId!!,
            amount = encData.amount,
            responseCode = encData.responseCode,
            message = encData.message
        )

        whenever(decrypter.decrypt<EncData>(any(), any())).thenReturn(encData)
        whenever(repository.save(any())).thenReturn(anyPayment())

        val response = sut.update(anyPaymentInformation())


        assertEquals(expectedResponse, response)

        // Check some fields with captor
        verify(repository).save(capture(paymentCaptor))
        val payment = paymentCaptor.value
        assertNotNull(paymentUpdated)
        assertEquals(PaymentStatus.PAID, payment.status)

        verify(decrypter).decrypt<EncData>(any(), any())
        verify(wabiPaymentAsyncNotificationSdk).notify(paymentUpdated)
    }

    @Test
    fun `Given an encData with invalid payment method should throw InvalidPaymentMethod exception`() {
        val encData = anyEncData(SUCCESS_RESPONSE_CODE, paymentMethod = randomString())

        whenever(decrypter.decrypt<EncData>(any(), any())).thenReturn(encData)

        assertFailsWith<InvalidPaymentMethodException> {
            sut.update(anyPaymentInformation())
        }

        verify(decrypter).decrypt<EncData>(any(), any())
        verifyNoInteractions(wabiPaymentAsyncNotificationSdk)
        verifyNoInteractions(repository)

    }

    private fun anyPayment() = Payment(
        paymentId = randomLong(),
        amount = randomBigDecimal(),
        paymentOption = randomString(),
        responseCode = randomString(),
        message = randomString(),
        encData = randomString(),
        status = PaymentStatus.PAID,
        supplierOrderId = randomLong()
    )

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
