import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
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
    queryParameters: Map<String, String> = emptyMap(),
): APIGatewayProxyRequestEvent = APIGatewayProxyRequestEvent()
    .withPath(path)
    .withHeaders(mapOf("Authorization" to authorization))
    .withQueryStringParameters(queryParameters)
    .withHttpMethod(method.name)
