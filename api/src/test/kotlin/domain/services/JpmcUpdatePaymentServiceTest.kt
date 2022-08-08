package domain.services

import adapters.repositories.jpmc.JpmcPaymentRepository
import com.wabi2b.jpmc.sdk.security.cipher.aes.decrypt.AesDecrypterService
import com.wabi2b.jpmc.sdk.usecase.sale.EncData
import domain.model.JpmcPayment
import domain.model.JpmcPaymentInformation
import domain.model.PaymentStatus
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

@ExtendWith(MockitoExtension::class)
class JpmcUpdatePaymentServiceTest {

    @Mock
    private lateinit var decrypter: AesDecrypterService

    @Mock
    private lateinit var jsonMapper: Json

    @Mock
    private lateinit var repository: JpmcPaymentRepository

    @InjectMocks
    private lateinit var sut: JpmcUpdatePaymentService

    @Test
    fun `given a valid encData when update then save information in database`() {
        // FIXME Missing to implement the events part

        val encData = anyEncData()
        whenever(decrypter.decrypt<EncData>(any(), any())).thenReturn(encData)
        whenever(repository.save(any())).thenReturn(anyJpmcPayment())

        val response = sut.update(anyPaymentInformation())

        assertAll(
            "Check values",
            { assertEquals(encData.txnRefNo, response.paymentId) },
            { assertEquals(encData.supplierOrderId, response.supplierOrderId) },
            { assertEquals(encData.amount, response.amount) },
            { assertEquals(encData.totalAmount, response.totalAmount) },
            { assertEquals(encData.responseCode, response.responseCode) },
            { assertEquals(encData.message, response.message) },
        )

        verify(decrypter).decrypt<EncData>(any(), any())
        verify(repository).save(any())
    }

    private fun anyJpmcPayment() = JpmcPayment(
        txnRefNo = randomString(),
        amount = randomString(),
        totalAmount = randomString(),
        paymentOption = randomString(),
        responseCode = randomString(),
        message = randomString(),
        encData = randomString(),
        status = PaymentStatus.PAID,
        supplierOrderId = randomString()
    )

    private fun anyPaymentInformation() = JpmcPaymentInformation(
        encData = randomString()
    )

    private fun anyEncData() = EncData(
        txnRefNo = randomString(),
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
        supplierOrderId = randomString(),
        totalAmount = randomString()
    )
}
