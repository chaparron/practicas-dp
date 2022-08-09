package domain.services

import adapters.repositories.jpmc.JpmcPaymentRepository
import com.wabi2b.jpmc.sdk.security.cipher.aes.decrypt.AesDecrypterService
import com.wabi2b.jpmc.sdk.security.formatter.PayloadFormatter
import com.wabi2b.jpmc.sdk.usecase.sale.EncData
import domain.model.JpmcPayment
import domain.model.JpmcPaymentInformation
import domain.model.PaymentStatus
import domain.model.UpdatePaymentResponse
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

class JpmcUpdatePaymentService(
    private val decrypter: AesDecrypterService,
    private val jsonMapper: Json,
    private val repository: JpmcPaymentRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun update(information: JpmcPaymentInformation): UpdatePaymentResponse {
        val encData = decrypter.decrypt<EncData>(information.encData) { PayloadFormatter(jsonMapper).decodeFrom(it) }
        logger.trace("encData: $encData")

        repository.save(
            JpmcPayment(
                txnRefNo = encData.txnRefNo,
                supplierOrderId = encData.supplierOrderId!!,
                amount = encData.amount,
                paymentOption = encData.paymentOption,
                responseCode = encData.responseCode,
                message = encData.message,
                encData = information.encData,
                status = if (encData.responseCode == "00") PaymentStatus.PAID else PaymentStatus.ERROR,
            )
        )
        logger.debug("The payment ${encData.txnRefNo} was successfully saved to the database for supplierOrderId ${encData.supplierOrderId}")

        // FIXME Missing to implement the events part

        return UpdatePaymentResponse(
            paymentId = encData.txnRefNo,
            supplierOrderId = encData.supplierOrderId!!,
            amount = encData.amount,
            responseCode = encData.responseCode,
            message = encData.message
        )
    }
}
