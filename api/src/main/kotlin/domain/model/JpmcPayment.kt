package domain.model

data class JpmcPayment(
    val supplierOrderId: String,
    val txnRefNo: String,
    val totalAmount: String,
    val amount: String,
    val paymentOption: String? = null,
    val responseCode: String? = null,
    val message: String? = null,
    val encData: String? = null,
    val status: PaymentStatus
)

enum class PaymentStatus {
    IN_PROGRESS, PAID, ERROR
}
