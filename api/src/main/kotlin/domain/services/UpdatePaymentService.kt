package domain.services

import adapters.repositories.jpmc.JpmcPaymentRepository
import com.wabi2b.jpmc.sdk.usecase.sale.PaymentData
import com.wabi2b.jpmc.sdk.usecase.sale.PaymentService
import com.wabi2b.jpmc.sdk.usecase.sale.PaymentStatus
import com.wabi2b.jpmc.sdk.usecase.sale.EncData
import domain.model.JpmcPaymentInformation
import domain.model.PaymentForUpdate
import kotlinx.serialization.json.Json
import domain.model.UpdatePaymentResponse
import org.slf4j.LoggerFactory
import wabi2b.payment.async.notification.sdk.WabiPaymentAsyncNotificationSdk
import wabi2b.payments.common.model.dto.PaymentUpdated
import java.time.Instant

class UpdatePaymentService(
    private val paymentService: PaymentService,
    private val repository: JpmcPaymentRepository,
    private val wabiPaymentAsyncNotificationSdk: WabiPaymentAsyncNotificationSdk
) {

    companion object {
        private val logger = LoggerFactory.getLogger(UpdatePaymentService::class.java)
        private const val SUCCESS_RESPONSE_CODE = "00"
    }

    fun update(information: JpmcPaymentInformation): UpdatePaymentResponse = information.encData
        .runCatching {
            paymentService.createPaymentData(this)
        }.onSuccess {
            logger.info("Payment provider return the following response: $it")
            wabiPaymentAsyncNotificationSdk.notify(it.toPaymentUpdated())
            repository.update(it.toPaymentForUpdate())
        }.onFailure {
            logger.error("There was an error decrypting provider response: $it")
        }.getOrThrow().toUpdatePaymentResponse()

    private fun PaymentData.toUpdatePaymentResponse() = UpdatePaymentResponse(
        paymentId = paymentId,
        supplierOrderId = supplierOrderId,
        amount = amount,
        responseCode = responseCode,
        message = message
    )

    private fun PaymentData.toPaymentForUpdate() = PaymentForUpdate(
        paymentId = paymentId,
        paymentOption = paymentOption,
        responseCode = responseCode,
        message = message,
        encData = encData,
        status = enumValueOf<PaymentStatus>(status.name),
        lastUpdatedAt = Instant.now().toString()
    )

    private fun PaymentData.toPaymentUpdated() = PaymentUpdated(
        supplierOrderId = supplierOrderId,
        paymentType = paymentType,
        amount = amount,
        paymentId = paymentId,
        paymentMethod = paymentMethod,
        resultType = resultType
    )
}

data class InvalidPaymentMethodException(val paymentMethod: String) : RuntimeException("Invalid payment method: $paymentMethod")

