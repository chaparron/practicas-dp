package digitalpayments.sdk.builders.apiResponse

import domain.model.errors.DpException
import domain.model.errors.DpErrorReason
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import wabi.sdk.impl.DetailedError

object ErrorResponseBuilder {

    fun buildApiDetailedError(
        reason: DpErrorReason = DpErrorReason.UNKNOWN,
        detail: String? = "Dummy detail"
    ) = DetailedError(reason.name, detail)

    fun buildApiRequestErrorResponse(reason: DpErrorReason, property: String) =
        buildApiRequestErrorResponse(DpException.from(reason = reason, detailItem = property))

    private fun buildApiRequestErrorResponse(exception: DpException) =
        ErrorResponse(listOf(exception.toError()))

    private fun DpException.toError() = DpError(
        type = this.reason.name,
        entity = "",
        property = "",
        invalidValue = "",
        message = this.message ?: ""
    )

    fun ErrorResponse.toSdkResponse() = ErrorResponse(
        errors = this.errors.map {
            DpError(
                type = it.type,
                entity = it.entity,
                property = it.property,
                invalidValue = it.invalidValue,
                message = it.message
            )
        }
    )

    fun DetailedError.toJson(json: Json) = mapOf(
        "reason" to reason,
        "detail" to detail
    ).let { json.encodeToString(it) }

}

@Serializable
data class ErrorResponse(val errors: List<DpError>)

@Serializable
data class DpError(
    val type: String? = null,
    val entity: String,
    val property: String,
    val invalidValue: String,
    val message: String
)



