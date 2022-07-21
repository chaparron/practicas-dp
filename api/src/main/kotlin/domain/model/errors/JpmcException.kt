package domain.model.errors

open class JpmcException(
    val reason: JpmcErrorReason,
    val detail: String,
    val rootCause: Throwable? = null
) : RuntimeException(rootCause) {

    companion object {

        fun from(reason: JpmcErrorReason, rootCause: Throwable? = null, detailItem: Any? = null) = JpmcException(
            reason = reason,
            detail = reason.detail(detailItem),
            rootCause = rootCause
        )

        fun unknown(detail: String = "Unknown", cause: Throwable? = null) = JpmcException(
            reason = JpmcErrorReason.UNKNOWN,
            detail = detail,
            rootCause = cause
        )
    }

    open val errorMessage = (rootCause?.message ?: rootCause?.javaClass?.simpleName ?: "Unknown error. ")
        .plus("[$detail]")

}
