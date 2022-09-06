package domain.model.exceptions

import domain.model.exceptions.ErrorReason.DetailBuilder.entityNotFound
import domain.model.exceptions.ErrorReason.DetailBuilder.invalid
import domain.model.exceptions.ErrorType.BAD_REQUEST

enum class ErrorReason(
    val type: ErrorType,
    private val pattern: (Any?) -> String
) {

    // Project exceptions
    FUNCTIONALITY_NOT_AVAILABLE(BAD_REQUEST, invalid("StateValidator")),
    CLIENT_TOKEN_EXCEPTION(BAD_REQUEST, invalid("clientToken")),
    MISSING_FIELD_EXCEPTION(BAD_REQUEST, invalid("MissingField")),
    UPDATE_PAYMENT_EXCEPTION(BAD_REQUEST, invalid("UpdatePayment")),
    PAYMENT_NOT_FOUND_EXCEPTION(BAD_REQUEST, invalid("JpmcPayment")),
    UNKNOWN(ErrorType.UNKNOWN, { "Unknown error" } ),

    // wabi2b-payments exceptions
    TOTAL_AMOUNT_REACHED(BAD_REQUEST, { "Total amount reached" } ),
    SUPPLIER_ORDER_NOT_FOUND(ErrorType.NOT_FOUND, entityNotFound("supplierOrderId"));


    fun detail(detailItem: Any? = null) = pattern(detailItem)

    private object DetailBuilder {

        fun missingProperty(entity: String) = { _: Any? -> "Missing property $entity" }

        fun entityNotFound(entity: String) = { value: Any? -> "$entity $value not found" }

        fun invalid(entity: String) = { value: Any? -> "Invalid $entity $value" }

    }
    companion object{
        fun find(externalReason: String) = values().firstOrNull { externalReason == it.name }?: UNKNOWN
    }
}
