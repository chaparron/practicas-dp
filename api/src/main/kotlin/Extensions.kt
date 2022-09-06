import configuration.MainConfiguration
import domain.services.InvalidPaymentMethodException
import kotlinx.serialization.encodeToString
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import wabi2b.payments.common.model.dto.type.PaymentMethod

fun String.toAttributeValue(): AttributeValue = AttributeValue.builder().s(this).build()
fun Long.toAttributeValue(): AttributeValue = AttributeValue.builder().s(this.toString()).build()

inline fun <reified T> T.asJsonString(): String = MainConfiguration.jsonMapper.encodeToString(this)

fun String.toPaymentMethod(): PaymentMethod {
    return when (this) {

        "cc" -> PaymentMethod.CREDIT_CARD
        "dc" -> PaymentMethod.DEBIT_CARD
        "nb" -> PaymentMethod.NET_BANKING
        "upi" -> PaymentMethod.UPI
        "wt" -> PaymentMethod.DIGITAL_WALLET
        else -> throw InvalidPaymentMethodException(this)
    }
}
