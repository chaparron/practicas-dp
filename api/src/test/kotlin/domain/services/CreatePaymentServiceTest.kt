package domain.services

import adapters.repositories.jpmc.JpmcPaymentRepository
import anyCreatePaymentRequest
import com.wabi2b.jpmc.sdk.usecase.sale.SaleInformation
import com.wabi2b.jpmc.sdk.usecase.sale.SaleService
import configuration.EnvironmentVariable
import domain.model.CreatePaymentRequest
import domain.model.JpmcPayment
import domain.model.PaymentStatus
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
import org.mockito.internal.verification.Times
import org.mockito.kotlin.times
import reactor.core.publisher.Mono
import wabi2b.payments.common.model.dto.PaymentId
import wabi2b.payments.common.model.dto.PaymentRequestDto
import wabi2b.payments.common.model.dto.PaymentType
import wabi2b.payments.common.model.dto.StartPaymentRequestDto
import wabi2b.payments.sdk.client.impl.WabiPaymentSdk

@ExtendWith(MockitoExtension::class)
class CreatePaymentServiceTest {

    @Mock
    private lateinit var saleServiceSdk: SaleService

    @Mock
    private lateinit var configuration: EnvironmentVariable.JpmcConfiguration

    @Mock
    private lateinit var jpmcRepository: JpmcPaymentRepository

    @Mock
    private lateinit var tokenProvider: TokenProvider

    @Mock
    private lateinit var paymentSdk: WabiPaymentSdk

    @InjectMocks
    private lateinit var sut: JpmcCreatePaymentService

    @Test
    fun `given a valid request when createPayment then return valid information and persist in DB`() {
        val saleInformation = anySaleInformation()
        val anyPaymentId = PaymentId(100L)
        val request = anyCreatePaymentRequest()
        val paymentSdkRequest = request.toStartPaymentRequestDto()
        val accessToken = randomString()

        whenever(paymentSdk.startPayment(any(), any())).thenReturn(Mono.just(anyPaymentId))
        whenever(saleServiceSdk.getSaleInformation(any())).thenReturn(saleInformation)
        whenever(jpmcRepository.save(any())).thenReturn(anyJpmcPayment())
        whenever(tokenProvider.getClientToken()).thenReturn(accessToken)
        wheneverForConfigurations()


        val response = sut.createPayment(request)

        assertAll(
            "Check values",
            { assertEquals(saleInformation.bankId, response.bankId) },
            { assertEquals(saleInformation.merchantId, response.merchantId) },
            { assertEquals(saleInformation.terminalId, response.terminalId) },
            { assertEquals(saleInformation.encData, response.encData) },
        )

        verify(saleServiceSdk).getSaleInformation(any())
        verify(jpmcRepository).save(any())
        verify(tokenProvider).getClientToken()
        verify(paymentSdk, times(1)).startPayment(paymentSdkRequest, accessToken)
        verifyForConfigurations()
    }

    private fun anyJpmcPayment() = JpmcPayment(
        supplierOrderId = randomString(),
        txnRefNo = randomString(),
        amount = "40",
        status = PaymentStatus.IN_PROGRESS
    )


    private fun anySaleInformation() = SaleInformation(
        bankId = "001002",
        merchantId = "100000000010588",
        terminalId = "10010186",
        encData = "0123456789abcdefgh"
    )

    private fun wheneverForConfigurations() {
        whenever(configuration.version).thenReturn("")
        whenever(configuration.passCode).thenReturn("")
        whenever(configuration.bankId).thenReturn("")
        whenever(configuration.terminalId).thenReturn("")
        whenever(configuration.merchantId).thenReturn("")
        whenever(configuration.mcc).thenReturn("")
        whenever(configuration.currency).thenReturn("")
        whenever(configuration.returnUrl).thenReturn("")
    }

    private fun verifyForConfigurations() {
        verify(configuration).bankId
        verify(configuration).terminalId
        verify(configuration).version
        verify(configuration).passCode
        verify(configuration).merchantId
        verify(configuration).mcc
        verify(configuration).currency
        verify(configuration).returnUrl
    }

    private fun CreatePaymentRequest.toStartPaymentRequestDto() = StartPaymentRequestDto(
        supplierOrderId = supplierOrderId,
        paidAmount = amount.toBigDecimal(),
        paymentType = PaymentType.DIGITAL_PAYMENT
    )
}
