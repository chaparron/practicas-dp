package domain.services

import com.wabi2b.jpmc.sdk.usecase.sale.SaleRequest
import com.wabi2b.jpmc.sdk.usecase.sale.SaleService
import configuration.EnvironmentVariable.JpmcConfiguration
import domain.model.CreatePaymentRequest
import domain.model.CreatePaymentResponse
import domain.model.errors.FunctionalityNotAvailable
import adapters.repositories.jpmc.JpmcPaymentRepository
import com.wabi2b.jpmc.sdk.usecase.sale.SaleInformation
import domain.model.JpmcPayment
import domain.model.PaymentStatus
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import wabi2b.payments.common.model.dto.PaymentType
import wabi2b.payments.common.model.dto.StartPaymentRequestDto
import wabi2b.payments.sdk.client.impl.WabiPaymentSdk

class CreatePaymentService(
    private val saleServiceSdk: SaleService,
    private val configuration: JpmcConfiguration,
    private val jpmcRepository: JpmcPaymentRepository,
    private val tokenProvider: TokenProvider,
    private val paymentSdk: WabiPaymentSdk,
    private val paymentExpirationService: PaymentExpirationService
) {
    companion object {
        private const val TRANSACTION_TYPE = "Pay"
        private val logger = LoggerFactory.getLogger(CreatePaymentService::class.java)
    }

    @Throws(FunctionalityNotAvailable::class)
    fun createPayment(request: CreatePaymentRequest): CreatePaymentResponse {
        return CreatePaymentContext(
            accessToken = tokenProvider.getClientToken(),
            request = request
        ).async().doOnSuccess {
            logger.trace("Create payment initialized with client token: ${it.accessToken.obfuscate()}")
        }.zipWhen {
            retrievePaymentId(it)
        }.map {
            doCreatePayment(it.t2, it.t1)
        }.blockOptional().orElseThrow { IllegalStateException("response for $request must not be empty") }
    }

    private fun doCreatePayment(paymentId: String, context: CreatePaymentContext): CreatePaymentResponse {
        return paymentExpirationService.init(paymentId).runCatching {
            saleServiceSdk.getSaleInformation(buildRequest(context.request, paymentId)).toCreatePaymentResponse()
        }.onSuccess {
            logger.trace("Payment created: $it")
            jpmcRepository.save(
                JpmcPayment(
                    supplierOrderId = context.request.supplierOrderId,
                    txnRefNo = paymentId,
                    amount = context.request.amount,
                    status = PaymentStatus.IN_PROGRESS
                )
            )
        }.onFailure {
            logger.error("There was an error starting paymentId $paymentId. Exception: $it")
        }.getOrThrow()
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
        supplierOrderId = request.supplierOrderId
    )

    private fun SaleInformation.toCreatePaymentResponse() = CreatePaymentResponse(
        bankId = bankId,
        merchantId = merchantId,
        terminalId = terminalId,
        encData = encData
    )

    private fun CreatePaymentRequest.toStartPaymentRequestDto() = StartPaymentRequestDto(
        supplierOrderId = supplierOrderId,
        paidAmount = amount.toBigDecimal(),
        paymentType = PaymentType.DIGITAL_PAYMENT
    )

    private fun retrievePaymentId(context: CreatePaymentContext): Mono<String> {
        return paymentSdk.startPayment(context.request.toStartPaymentRequestDto(), context.accessToken).map {
            it.value.toString()
        }
    }

    private fun String.obfuscate() = "${take(4)}..${takeLast(4)}"

    data class CreatePaymentContext(
        val accessToken: String,
        val request: CreatePaymentRequest
    ) {
        fun async(): Mono<CreatePaymentContext> = Mono.just(this)
    }
}
