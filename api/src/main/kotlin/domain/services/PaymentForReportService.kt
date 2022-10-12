package domain.services

import adapters.repositories.paymentforreport.DynamoDBPaymentForReportRepository
import com.wabi2b.jpmc.sdk.usecase.sale.PaymentData
import domain.model.PaymentForReport
import org.slf4j.LoggerFactory
import java.time.Clock
import java.util.*

interface PaymentForReportService {
    fun save(paymentData: PaymentData): PaymentForReport
    fun get(paymentId: Long): PaymentForReport
}

class DefaultPaymentForReportService(
    private val paymentForReportRepository: DynamoDBPaymentForReportRepository,
    private val clock: Clock,
    private val reportDateService: ReportDateService
) : PaymentForReportService {

    companion object {
        private val logger = LoggerFactory.getLogger(DefaultPaymentForReportService::class.java)
    }

    override fun save(paymentData: PaymentData): PaymentForReport {
        logger.info("About to transform the following paymentData $paymentData to a paymentForReport")
        val paymentForReport = paymentData.toPaymentForReport()
        logger.info("About to save the following paymentForReport $paymentForReport")
        return paymentForReportRepository.save(paymentForReport)
    }

    override fun get(paymentId: Long): PaymentForReport {
        logger.info("About to get paymentForReport for $paymentId")
        return paymentForReportRepository.get(paymentId)
    }

    private fun PaymentData.toPaymentForReport(): PaymentForReport {
        val clockedInstant = clock.instant()
        return PaymentForReport(
            createdAt = Date.from(clockedInstant),
            reportDay = reportDateService.reportDate(clockedInstant),
            paymentId = paymentId,
            supplierOrderId = supplierOrderId,
            amount = amount,
            paymentOption = paymentOption,
            encData = encData,
            paymentType = paymentType,
            paymentMethod = paymentMethod
        )
    }
}
