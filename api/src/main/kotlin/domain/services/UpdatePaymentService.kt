package domain.services

import adapters.repositories.jpmc.JpmcPaymentRepository
import com.wabi2b.jpmc.sdk.security.cipher.aes.decrypt.AesDecrypterService
import com.wabi2b.jpmc.sdk.security.formatter.PayloadFormatter
import com.wabi2b.jpmc.sdk.usecase.sale.EncData
import domain.model.Payment
import domain.model.JpmcPaymentInformation
import domain.model.PaymentStatus
import domain.model.UpdatePaymentResponse
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import wabi2b.payment.async.notification.sdk.WabiPaymentAsyncNotificationSdk
import wabi2b.payments.common.model.dto.PaymentType
import wabi2b.payments.common.model.dto.PaymentUpdated
import wabi2b.payments.common.model.dto.type.PaymentResult

class UpdatePaymentService(
    private val decrypter: AesDecrypterService,
    private val jsonMapper: Json,
    private val repository: JpmcPaymentRepository,
    private val wabiPaymentAsyncNotificationSdk: WabiPaymentAsyncNotificationSdk
) {
    companion object {
        private val logger = LoggerFactory.getLogger(javaClass)
        private const val SUCCESS_RESPONSE_CODE = "00"
    }

    fun update(information: JpmcPaymentInformation): UpdatePaymentResponse = information.encData
        .runCatching {
            decrypter.decrypt<EncData>(this) { PayloadFormatter(jsonMapper).decodeFrom(it) }
        }.onSuccess {
            logger.info("Payment provider return the following response: $it")
            wabiPaymentAsyncNotificationSdk.notify(it.toPaymentUpdated())
            repository.save(it.toPayment(information.encData))
        }.onFailure {
            logger.error("There was an error decrypting provider response: $it")
        }.getOrThrow().toUpdatePaymentResponse()

    private fun EncData.toUpdatePaymentResponse() = UpdatePaymentResponse(
        paymentId = txnRefNo,
        supplierOrderId = supplierOrderId!!,
        amount = amount,
        responseCode = responseCode,
        message = message
    )

    private fun EncData.toPayment(encData: String) = Payment(
        paymentId = txnRefNo,
        supplierOrderId = supplierOrderId!!,
        amount = amount,
        paymentOption = paymentOption,
        responseCode = responseCode,
        message = message,
        encData = encData,
        status = if (responseCode == "00") PaymentStatus.PAID else PaymentStatus.ERROR,
    )
    private fun EncData.toPaymentUpdated() = PaymentUpdated(
        supplierOrderId = supplierOrderId!!.toLong(),
        paymentType = PaymentType.DIGITAL_PAYMENT,
        paymentId = txnRefNo.toLong(),
        resultType = if (responseCode == SUCCESS_RESPONSE_CODE) PaymentResult.SUCCESS else PaymentResult.FAILED
    )
}
