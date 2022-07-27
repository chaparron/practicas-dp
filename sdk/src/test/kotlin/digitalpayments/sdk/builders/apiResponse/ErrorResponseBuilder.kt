package digitalpayments.sdk.builders.apiResponse

import domain.model.errors.JpmcException
import domain.model.errors.JpmcErrorReason
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import wabi.sdk.impl.DetailedError

object ErrorResponseBuilder {

    fun buildApiDetailedError(
        reason: JpmcErrorReason = JpmcErrorReason.UNKNOWN,
        detail: String? = "Dummy detail"
    ) = DetailedError(reason.name, detail)

    fun buildApiRequestErrorResponse(reason: JpmcErrorReason, property: String) =
        buildApiRequestErrorResponse(JpmcException.from(reason = reason, detailItem = property))

    private fun buildApiRequestErrorResponse(exception: JpmcException) =
        ErrorResponse(listOf(exception.toError()))

    private fun JpmcException.toError() = JpmcError(
        type = this.reason.name,
        entity = "",
        property = "",
        invalidValue = "",
        message = this.message ?: ""
    )

    fun ErrorResponse.toSdkResponse() = ErrorResponse(
        errors = this.errors.map {
            JpmcError(
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
data class ErrorResponse(val errors: List<JpmcError>)

@Serializable
data class JpmcError(
    val type: String? = null,
    val entity: String,
    val property: String,
    val invalidValue: String,
    val message: String
)



