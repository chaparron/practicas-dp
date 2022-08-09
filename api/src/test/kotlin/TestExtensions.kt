import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import domain.model.CreatePaymentRequest
import domain.model.Supplier
import org.springframework.http.HttpMethod
import java.util.UUID

fun randomString() = UUID.randomUUID().toString()

fun anySupplier() = Supplier(
    supplierId = randomString(),
    state = randomString(),
    bankAccountNumber = randomString(),
    indianFinancialSystemCode = randomString()
)
fun apiGatewayEventRequest(
    method: HttpMethod = HttpMethod.POST,
    path: String? = randomString(),
    authorization: String? = randomString(),
    body: String = randomString()
): APIGatewayProxyRequestEvent = APIGatewayProxyRequestEvent()
    .withPath(path)
    .withHeaders(mapOf("Authorization" to authorization))
    .withBody(body)
    .withHttpMethod(method.name)

fun anyCreatePaymentRequest() = CreatePaymentRequest(
    supplierOrderId = randomString(),
    amount = "100"
)
