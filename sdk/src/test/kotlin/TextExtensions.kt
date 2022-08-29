import digitalpayments.sdk.model.CreatePaymentRequest
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID
import kotlin.random.Random

fun randomBigDecimal(): BigDecimal = BigDecimal(Random.nextDouble()).setScale(2, RoundingMode.HALF_EVEN)
fun randomLong() = Random.nextLong()
fun randomString() = UUID.randomUUID().toString()
fun anyCreatePaymentRequest() = CreatePaymentRequest(
    supplierOrderId = randomLong(),
    amount = randomBigDecimal(),
    invoiceId = randomString()
)
