package domain.model.errors

import domain.model.errors.ErrorType.BAD_REQUEST
import domain.model.errors.JpmcErrorReason.DetailBuilder.missingProperty

enum class JpmcErrorReason(
    val type: ErrorType,
    private val pattern: (Any?) -> String
) {
    MISSING_AMOUNT(BAD_REQUEST, missingProperty("amount")),
    UNKNOWN(ErrorType.UNKNOWN, { "Unexpected error occur" });

    fun detail(detailItem: Any? = null) = pattern(detailItem)

    private object DetailBuilder {

        fun missingProperty(entity: String) = { _: Any? -> "Missing property $entity" }

        fun entityNotFound(entity: String) = { value: Any? -> "$entity $value not found" }

        fun invalid(entity: String) = { value: Any? -> "Invalid $entity $value" }

    }
}
