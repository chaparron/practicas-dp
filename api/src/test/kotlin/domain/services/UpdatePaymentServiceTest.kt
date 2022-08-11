package domain.services

import adapters.repositories.jpmc.JpmcPaymentRepository
import com.wabi2b.jpmc.sdk.security.cipher.aes.decrypt.AesDecrypterService
import com.wabi2b.jpmc.sdk.usecase.sale.EncData
import domain.model.Payment
import domain.model.JpmcPaymentInformation
import domain.model.PaymentStatus
import kotlin.random.Random
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import randomString
import kotlin.test.assertEquals
import org.mockito.kotlin.times
import wabi2b.payment.async.notification.sdk.WabiPaymentAsyncNotificationSdk
import wabi2b.payments.common.model.dto.PaymentType
import wabi2b.payments.common.model.dto.PaymentUpdated
import wabi2b.payments.common.model.dto.type.PaymentResult

@ExtendWith(MockitoExtension::class)
class UpdatePaymentServiceTest {

    @Mock
    private lateinit var decrypter: AesDecrypterService

    @Mock
    private lateinit var jsonMapper: Json

    @Mock
    private lateinit var repository: JpmcPaymentRepository

    @Mock
    private lateinit var  wabiPaymentAsyncNotificationSdk: WabiPaymentAsyncNotificationSdk


    @InjectMocks
    private lateinit var sut: UpdatePaymentService

    companion object {
        private const val SUCCESS_RESPONSE_CODE = "00"
    }

    @Test
    fun `given a valid encData when update then save information in database`() {
        val encData = anyEncData()
        val paymentUpdated = PaymentUpdated(
            supplierOrderId = encData.supplierOrderId!!.toLong(),
            paymentType = PaymentType.DIGITAL_PAYMENT,
            paymentId = encData.txnRefNo.toLong(),
            resultType = if (encData.responseCode == SUCCESS_RESPONSE_CODE) PaymentResult.SUCCESS else PaymentResult.FAILED
        )
        whenever(decrypter.decrypt<EncData>(any(), any())).thenReturn(encData)
        whenever(repository.save(any())).thenReturn(anyPayment())

        val response = sut.update(anyPaymentInformation())

        assertAll(
            "Check values",
            { assertEquals(encData.txnRefNo, response.paymentId) },
            { assertEquals(encData.supplierOrderId, response.supplierOrderId) },
            { assertEquals(encData.amount, response.amount) },
            { assertEquals(encData.responseCode, response.responseCode) },
            { assertEquals(encData.message, response.message) },
        )

        verify(decrypter).decrypt<EncData>(any(), any())
        verify(repository).save(any())
        verify(wabiPaymentAsyncNotificationSdk, times(1)).notify(paymentUpdated)
    }

    private fun anyPayment() = Payment(
        paymentId = Random.nextLong().toString(),
        amount = randomString(),
        paymentOption = randomString(),
        responseCode = randomString(),
        message = randomString(),
        encData = randomString(),
        status = PaymentStatus.PAID,
        supplierOrderId = Random.nextLong().toString()
    )

    private fun anyPaymentInformation() = JpmcPaymentInformation(
        encData = randomString()
    )

    private fun anyEncData() = EncData(
        txnRefNo = Random.nextLong().toString(),
        merchantId = randomString(),
        amount = randomString(),
        terminalId = randomString(),
        responseCode = randomString(),
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
        paymentOption = randomString(),
        secureHash = randomString(),
        supplierOrderId = Random.nextLong().toString()
    )
}
