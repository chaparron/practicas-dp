package domain.services

import adapters.repositories.jpmc.DynamoDbJpmcPaymentRepository
import com.wabi2b.jpmc.sdk.usecase.sale.SaleRequest
import com.wabi2b.jpmc.sdk.usecase.sale.SaleService
import configuration.EnvironmentVariable.JpmcConfiguration
import domain.model.CreatePaymentRequest
import domain.model.CreatePaymentResponse
import domain.model.errors.FunctionalityNotAvailable
import java.util.*
import adapters.repositories.jpmc.JpmcPaymentRepository
import domain.model.JpmcPayment
import domain.model.PaymentStatus
import org.slf4j.LoggerFactory

class JpmcCreatePaymentService(
    private val saleServiceSdk: SaleService,
    private val configuration: JpmcConfiguration,
    private val jpmcRepository: JpmcPaymentRepository,
    private val tokenProvider: TokenProvider
) {
    companion object {
        private const val TRANSACTION_TYPE = "Pay"
        private val logger = LoggerFactory.getLogger(JpmcCreatePaymentService::class.java)
    }

    @Throws(FunctionalityNotAvailable::class)
    fun createPayment(request: CreatePaymentRequest): CreatePaymentResponse {

        val dpToken = tokenProvider.getClientToken()
        logger.debug("Client token: $dpToken")
        //FIXME We need obtain this value in task WM-1222
        val paymentId = UUID.randomUUID().toString()

        return saleServiceSdk.getSaleInformation(buildRequest(request, paymentId))
            .toCreatePaymentResponse()
            .also {
                jpmcRepository.save(
                    JpmcPayment(
                        supplierOrderId = request.supplierOrderId,
                        txnRefNo = paymentId,
                        totalAmount = request.totalAmount,
                        amount = request.amount,
                        status = PaymentStatus.IN_PROGRESS
                    )
                )
            }
    }

    private fun buildRequest(request: CreatePaymentRequest, paymentId: String) = SaleRequest(
        version = configuration.version,
        txnRefNo = paymentId,
        amount = request.amount,
        passCode = configuration.passCode, //TODO this passCode must be in a Secret
        bankId = configuration.bankId,
        terminalId = configuration.terminalId,
        merchantId = configuration.merchantId,
        mCC = configuration.mcc,
        currency = configuration.currency,
        txnType = TRANSACTION_TYPE,
        returnUrl = configuration.returnUrl,
        supplierOrderId = request.supplierOrderId,
        totalAmount = request.totalAmount
    )

    private fun com.wabi2b.jpmc.sdk.usecase.sale.SaleInformation.toCreatePaymentResponse() = CreatePaymentResponse(
        bankId = bankId,
        merchantId = merchantId,
        terminalId = terminalId,
        encData = encData
    )

}
