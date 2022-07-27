import software.amazon.awssdk.services.dynamodb.model.AttributeValue

fun String.toAttributeValue(): AttributeValue = AttributeValue.builder().s(this).build()
