package domain.model.errors

import domain.model.errors.ErrorType.BAD_REQUEST
import domain.model.errors.DpErrorReason.DetailBuilder.missingProperty

enum class DpErrorReason(
    val type: ErrorType,
    private val pattern: (Any?) -> String
) {
    MISSING_AMOUNT(BAD_REQUEST, missingProperty("amount")),
    MISSING_INVOICE_ID(BAD_REQUEST, missingProperty("invoiceId")),
    MISSING_SUPPLIER_ID(BAD_REQUEST, missingProperty("supplierId")),
    FUNCTIONALITY_NOT_AVAILABLE(BAD_REQUEST, { "This functionality is only available from Mumbai users" }),
    UNKNOWN(ErrorType.UNKNOWN, { "Unexpected error occur" });

    fun detail(detailItem: Any? = null) = pattern(detailItem)

    private object DetailBuilder {
        fun missingProperty(entity: String) = { _: Any? -> "Missing property $entity" }
    }
}
