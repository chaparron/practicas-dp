package domain.services

import com.wabi2b.jpmc.sdk.usecase.sale.SaleRequest
import com.wabi2b.jpmc.sdk.usecase.sale.SaleService
import configuration.EnvironmentVariable.JpmcConfiguration
import domain.model.CreatePaymentRequest
import domain.model.CreatePaymentResponse
import domain.model.errors.FunctionalityNotAvailable
import java.util.*

class JpmcCreatePaymentService(
    private val saleServiceSdk: SaleService,
    private val configuration: JpmcConfiguration
) {
    companion object {
        private const val TRANSACTION_TYPE = "Pay"
    }

    @Throws(FunctionalityNotAvailable::class)
    fun createPayment(request: CreatePaymentRequest): CreatePaymentResponse {
        return saleServiceSdk.getSaleInformation(buildRequest(request.amount)).toCreatePaymentResponse()
    }

    private fun buildRequest(amount: String) = SaleRequest(
        version = configuration.version,
        txnRefNo = UUID.randomUUID().toString(),
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
