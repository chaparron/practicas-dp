package domain.services

import adapters.repositories.jpmc.JpmcPaymentRepository
import com.wabi2b.jpmc.sdk.usecase.sale.PaymentStatus
import com.wabi2b.jpmc.sdk.usecase.sale.SaleInformation
import com.wabi2b.jpmc.sdk.usecase.sale.SaleRequest
import com.wabi2b.jpmc.sdk.usecase.sale.SaleService
import configuration.EnvironmentVariable.JpmcConfiguration
import domain.model.*
import domain.model.errors.FunctionalityNotAvailable
import org.joda.time.Instant
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import wabi2b.payments.common.model.dto.StartPaymentRequestDto
import wabi2b.payments.sdk.client.impl.WabiPaymentSdk
import java.math.BigDecimal

class CreatePaymentService(
    private val saleServiceSdk: SaleService,
    private val configuration: JpmcConfiguration,
    private val jpmcRepository: JpmcPaymentRepository,
    private val tokenProvider: TokenProvider,
    private val paymentSdk: WabiPaymentSdk,
    private val paymentExpirationService: PaymentExpirationService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(CreatePaymentService::class.java)
        private const val TRANSACTION_TYPE = "Pay"
    }

    @Throws(FunctionalityNotAvailable::class)
    fun createPayment(request: CreatePaymentRequest): CreatePaymentResponse {
        return CreatePaymentContext(
            accessToken = tokenProvider.getClientToken(),
            request = request
        ).async().doOnSuccess {
            validateAmount(it.request.amount)
            logger.trace("Create payment initialized with client token: ${it.accessToken.obfuscate()}")
        }.zipWhen {
            retrievePaymentId(it)
        }.map {
            doCreatePayment(it.t2, it.t1)
        }.blockOptional().orElseThrow { IllegalStateException("response for $request must not be empty") }
    }

    private fun validateAmount(amount: BigDecimal) {
        if (amount < BigDecimal(0) || amount > BigDecimal(500))
            throw IllegalArgumentException("You cannot use less than 0 or more than 500 amount")
    }
    private fun doCreatePayment(paymentId: Long, context: CreatePaymentContext): CreatePaymentResponse {
        return paymentExpirationService.init(
            PaymentExpiration(
                paymentId = paymentId,
                amount = context.request.amount,
                supplierOrderId = context.request.supplierOrderId
            )
        ).runCatching {
            saleServiceSdk.getSaleInformation(buildRequest(context.request, paymentId.toString()))
                .toCreatePaymentResponse()
        }.onSuccess {
            logger.trace("Payment created: $it")
            jpmcRepository.save(
                PaymentForSave(
                    supplierOrderId = context.request.supplierOrderId,
                    paymentId = paymentId,
                    amount = context.request.amount,
                    status = PaymentStatus.IN_PROGRESS,
                    invoiceId = context.request.invoiceId,
                    createdAt = Instant.now().toString(),
                    lastUpdatedAt = Instant.now().toString()
                )
            )
        }.onFailure {
            logger.error("There was an error starting paymentId $paymentId. Exception: $it")
        }.getOrThrow()
    }

    private fun buildRequest(request: CreatePaymentRequest, paymentId: String) = SaleRequest(
        version = configuration.version,
        paymentId = paymentId,
        amount = request.amount.toString(),
        passCode = configuration.passCode,
        bankId = configuration.bankId,
        terminalId = configuration.terminalId,
        merchantId = configuration.merchantId,
        mcc = configuration.mcc,
        currency = configuration.currency,
        txnType = TRANSACTION_TYPE,
        returnUrl = configuration.returnUrl,
        supplierOrderId = request.supplierOrderId.toString()
    )

    private fun SaleInformation.toCreatePaymentResponse() = CreatePaymentResponse(
        bankId = bankId,
        merchantId = merchantId,
        terminalId = terminalId,
        encData = encData
    )

    private fun CreatePaymentRequest.toStartPaymentRequestDto() = StartPaymentRequestDto(
        supplierOrderId = supplierOrderId,
        amount = amount
    )

    private fun retrievePaymentId(context: CreatePaymentContext): Mono<Long> =
        paymentSdk.startPayment(context.request.toStartPaymentRequestDto(), context.accessToken).map {
            it.value
        }.doOnSuccess {
            logger.info("Payment was successfully initiated with Payment ID $it")
        }.doOnError {
            logger.error("An error has occurred initiating the payment. $it")
        }

    private fun String.obfuscate() = "${take(4)}..${takeLast(4)}"

    data class CreatePaymentContext(
        val accessToken: String,
        val request: CreatePaymentRequest
    ) {
        fun async(): Mono<CreatePaymentContext> = Mono.just(this)
    }
}
