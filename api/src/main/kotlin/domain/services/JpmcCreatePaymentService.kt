package domain.services

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

class JpmcCreatePaymentService(
    private val saleServiceSdk: SaleService,
    private val configuration: JpmcConfiguration,
    private val jpmcRepository: JpmcPaymentRepository
) {
    companion object {
        private const val TRANSACTION_TYPE = "Pay"
    }

    @Throws(FunctionalityNotAvailable::class)
    fun createPayment(request: CreatePaymentRequest): CreatePaymentResponse {

        //FIXME We need obtain this value in task WM-1222
        val paymentId = UUID.randomUUID().toString()

        return saleServiceSdk.getSaleInformation(buildRequest(request.amount, paymentId))
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

    private fun buildRequest(amount: String, paymentId: String) = SaleRequest(
        version = configuration.version,
        txnRefNo = paymentId,
        amount = amount,
        passCode = configuration.passCode, //TODO this passCode must be in a Secret
        bankId = configuration.bankId,
        terminalId = configuration.terminalId,
        merchantId = configuration.merchantId,
        mCC = configuration.mcc,
        currency = configuration.currency,
        txnType = TRANSACTION_TYPE,
        returnUrl = configuration.returnUrl
    )

    private fun com.wabi2b.jpmc.sdk.usecase.sale.SaleInformation.toCreatePaymentResponse() = CreatePaymentResponse(
        bankId = bankId,
        merchantId = merchantId,
        terminalId = terminalId,
        encData = encData
    )

}
