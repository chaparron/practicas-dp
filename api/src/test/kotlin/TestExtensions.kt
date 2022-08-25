import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import domain.model.CreatePaymentRequest
import domain.model.PaymentExpiration
import domain.model.Supplier
import java.math.BigDecimal
import java.math.RoundingMode
import org.mockito.ArgumentCaptor
import org.springframework.http.HttpMethod
import java.util.UUID
import kotlin.random.Random
import wabi2b.dtos.customers.shared.AddressDto
import wabi2b.dtos.customers.shared.CustomerDto
import wabi2b.dtos.customers.shared.DayDto
import wabi2b.dtos.customers.shared.HourRangeDto
import wabi2b.dtos.customers.shared.RatingScoreDto
import wabi2b.dtos.customers.shared.StoreType
import wabi2b.dtos.customers.shared.UserIdDto
import wabi2b.dtos.customers.shared.WorkingDaysDto

fun randomString() = UUID.randomUUID().toString()

fun randomBigDecimal(): BigDecimal = BigDecimal(Random.nextDouble()).setScale(2, RoundingMode.HALF_EVEN)

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

fun anyCustomer(addresses: List<AddressDto>) = CustomerDto(
    id = randomString(),
    name = randomString(),
    enabled = true,
    legalId = randomString(),
    customerStatus = randomString(),
    user = UserIdDto(Random.nextLong()),
    smsVerification = true,
    marketingEnabled = true,
    emailVerification = true,
    countryId = randomString(),
    customerTypeCode = randomString(),
    customerTypeName = randomString(),
    rating = RatingScoreDto(Random.nextFloat(), Random.nextInt(), Random.nextInt()),
    addresses = addresses,
    workingDays = WorkingDaysDto(listOf(DayDto(Random.nextInt(), true)), listOf(HourRangeDto(randomString(), randomString()))),
    storeType = StoreType.MAIN_OFFICE,
    permissionOnBranchOffice = true,
    verificationDocuments = null
)

fun anyPaymentExpiration() = PaymentExpiration(Random.nextLong(), randomBigDecimal())

@Suppress("UNCHECKED_CAST")
private fun <T> capture(captor: ArgumentCaptor<T>): T = captor.capture()
