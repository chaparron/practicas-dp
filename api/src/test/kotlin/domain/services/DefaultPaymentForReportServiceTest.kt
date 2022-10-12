package domain.services

import adapters.repositories.paymentforreport.DynamoDBPaymentForReportRepository
import com.wabi2b.jpmc.sdk.usecase.sale.PaymentData
import com.wabi2b.jpmc.sdk.usecase.sale.PaymentStatus
import domain.model.PaymentForReport
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import wabi2b.payments.common.model.dto.PaymentType
import wabi2b.payments.common.model.dto.type.PaymentMethod
import wabi2b.payments.common.model.dto.type.PaymentResult
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.util.*

@ExtendWith(MockitoExtension::class)
internal class DefaultPaymentForReportServiceTest {

    @Mock
    private lateinit var paymentForReportRepository: DynamoDBPaymentForReportRepository

    @Mock
    private lateinit var reportDateService: ReportDateService

    @Mock
    private lateinit var clock: Clock

    @InjectMocks
    private lateinit var sut: DefaultPaymentForReportService

    private val clockedInstant = Instant.now()

    //    private val paymentForReportRepository: DefaultPaymentForReportRepository = mock()
    //    private val sut = DefaultPaymentForReportService(paymentForReportRepository)

    // <editor-fold desc="vals anyPaymentData & anyPaymentForReport">
    private val anyPaymentData = PaymentData(
        paymentId = 77L,
        supplierOrderId = 77L,
        amount = BigDecimal(77),
        paymentOption = "String",
        responseCode = "String",
        message = "String",
        encData = "String",
        status = PaymentStatus.PAID,
        paymentType = PaymentType.DIGITAL_PAYMENT,
        paymentMethod = PaymentMethod.DIGITAL_WALLET,
        resultType = PaymentResult.SUCCESS
    )
    private val anyPaymentForReport = PaymentForReport(
        createdAt = Date.from(clockedInstant),
        reportDay = Date.from(clockedInstant),
        paymentId = 77L,
        supplierOrderId = 77L,
        amount = BigDecimal(77),
        paymentOption = "String",
        encData = "String",
        paymentType = PaymentType.DIGITAL_PAYMENT,
        paymentMethod = PaymentMethod.DIGITAL_WALLET
    )
    // </editor-fold>

    @Test
    fun `save should return PaymentForReportService giving a PaymentData`() {
        // Given
        val paymentData = anyPaymentData
        val paymentForReport = anyPaymentForReport
        // When
        whenever(reportDateService.reportDate(any())).thenReturn(Date.from(clockedInstant))
        whenever(paymentForReportRepository.save(paymentForReport)).thenReturn(paymentForReport)
        whenever(clock.instant()).thenReturn(clockedInstant)

        val actual = sut.save(paymentData)
        // Then
        assertEquals(paymentForReport, actual)
    }

    @Test
    fun `can retrieve saved supplier`() {
        // Given
        val paymentData = anyPaymentData
        val paymentForReport = anyPaymentForReport
        val paymentId = 77L
        // When
        whenever(reportDateService.reportDate(any())).thenReturn(Date.from(clockedInstant))
        whenever(paymentForReportRepository.save(paymentForReport)).thenReturn(paymentForReport)
        whenever(clock.instant()).thenReturn(clockedInstant)

        doReturn(paymentForReport).whenever(paymentForReportRepository).get(paymentId)

        sut.save(paymentData)

        val actual = sut.get(paymentId)
        // Then
        assertEquals(paymentForReport, actual)
    }
}
