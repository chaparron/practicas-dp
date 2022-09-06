import digitalpayments.sdk.model.CreatePaymentRequest
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID
import kotlin.random.Random

const val CUSTOM_EXCEPTION_HEADER = "X_Custom_Error"
const val CLIENT_TOKEN_PREFIX = "Bearer"
const val ACCESS_TOKEN = "dummy.jwt.token"

fun randomBigDecimal(): BigDecimal = BigDecimal(Random.nextDouble()).setScale(2, RoundingMode.HALF_EVEN)
fun randomLong() = Random.nextLong()
fun randomString() = UUID.randomUUID().toString()
fun anyCreatePaymentRequest() = CreatePaymentRequest(
    supplierOrderId = randomLong(),
    amount = randomBigDecimal(),
    invoiceId = randomString()
)
