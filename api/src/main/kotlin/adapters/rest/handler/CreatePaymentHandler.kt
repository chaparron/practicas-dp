package adapters.rest.handler

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import domain.model.CreatePaymentRequest
import domain.model.CreatePaymentResponse
import domain.services.CreatePaymentService
import domain.services.state.StateValidatorService
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import wabi.rest2lambda.RestHandler
import wabi.rest2lambda.ok
import java.math.BigDecimal

class CreatePaymentHandler(
    private val service: CreatePaymentService,
    private val jsonMapper: Json,
    private val stateValidatorService: StateValidatorService,
    private val createPaymentDummyEnabled: Boolean
) : RestHandler {

    companion object {
        private val logger = LoggerFactory.getLogger(CreatePaymentHandler::class.java)
        const val CREATE_PAYMENT_PATH = "/dp/jpmc/createPayment"
    }

    override fun handleRequest(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent {

        return if (createPaymentDummyEnabled) {
            ok(
                CreatePaymentResponse(
                    bankId = "000004",
                    merchantId = "101000000000781",
                    terminalId = "10100781",
                    encData = "N8EbKkM3TF+xqs6HTrEModyB4v3ACOKqwd/iMSsWVXeNZtenSlUdIKvwJiQfl53e+PVb074HpayaxLCRUghHjFHu6PF9f2DVH4et/q6HVynDCZnPHFNRvYxUnlX1kdmWE/BEle1//zze8bRTycnOE6hLkYj4Z12p0iAwF3T8DId0Riwd/z6tI1GTcjWwsSQibgPfHDIY//Vpz7jrP/pHEhZHlUWG2XIhhppDL+Tax5wtiZA9ac6L5HwiXudguQ0bngKcUEd+M/l6EvCZHL/ThA8ZT3r00RY5eqp2MBlji3k4+sI06/q9FWRAU7y0gxGfmc6Os+WtWaDmzVTLkW/AjVaLxgxXSxFgZYOO1WoTaoesM+pu4vuBo90xd4lI+wRiDBo3qk/3hlVVBKtsGREa2twJfoRWIcBj111oxbmRx7+DuPNByScNN6589how+k6Sqa+U6cp0y56ivlBVPZBOwFdW88OG2f1dWQd4Xs8p8EsKYiLnrkSPOq/bSt/8dtrY"
                )
                    .also {
                        logger.warn("Create payment Mock enabled!")
                    }
                    .let {
                        jsonMapper.encodeToString(it)
                    }
            )

        } else {
            logger.info("About to process the following input body: ${input.body}")
            stateValidatorService.validate(input)
            val request = jsonMapper.decodeFromString<CreatePaymentHandlerRequest>(input.body)
            ok(service.createPayment(request.toCreatePaymentRequest())
                .also {
                    logger.trace("Payment created: $it")
                }
                .let {
                    jsonMapper.encodeToString(it)
                }
            )
        }


    }

    private fun CreatePaymentHandlerRequest.toCreatePaymentRequest() = CreatePaymentRequest(
        supplierOrderId = supplierOrderId,
        amount = amount,
        invoiceId = invoiceId
    )

}
@Serializable
data class CreatePaymentHandlerRequest(
    val supplierOrderId: Long,
    @Contextual
    val amount: BigDecimal,
    val invoiceId: String
)
