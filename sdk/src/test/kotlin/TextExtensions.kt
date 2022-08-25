import digitalpayments.sdk.model.CreatePaymentRequest
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.random.Random

fun randomBigDecimal(): BigDecimal = BigDecimal(Random.nextDouble()).setScale(2, RoundingMode.HALF_EVEN)

fun randomLong() = Random.nextLong()

fun anyCreatePaymentRequest() = CreatePaymentRequest(
    supplierOrderId = randomLong(),
    amount = randomBigDecimal()
)
