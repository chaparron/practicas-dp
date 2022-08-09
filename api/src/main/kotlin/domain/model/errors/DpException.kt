package domain.model.errors

open class DpException(
    val reason: DpErrorReason,
    val detail: String,
    val rootCause: Throwable? = null
) : RuntimeException(rootCause) {

    companion object {
        fun from(reason: DpErrorReason, rootCause: Throwable? = null, detailItem: Any? = null) = DpException(
            reason = reason,
            detail = reason.detail(detailItem),
            rootCause = rootCause
        )
        fun unknown(detail: String = "Unknown", cause: Throwable? = null) = DpException(
            reason = DpErrorReason.UNKNOWN,
            detail = detail,
            rootCause = cause
        )
    }

}
