package adapters.repositories.jpmc

import domain.model.Payment
import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import toAttributeValue

interface JpmcPaymentRepository {
    fun save(payment: Payment): Payment
}

class DynamoDbJpmcPaymentRepository(
    private val dynamoDbClient: DynamoDbClient,
    private val tableName: String
) : JpmcPaymentRepository {

    companion object {
        private val logger = LoggerFactory.getLogger(DynamoDbJpmcPaymentRepository::class.java)
        private const val pkValuePrefix = "jpmc#"
    }

    override fun save(payment: Payment): Payment {
        return dynamoDbClient.putItem {
            logger.trace("Saving Jpmc payment $payment")
            it.tableName(tableName).item(payment.asDynamoItem())
        }.let {
            logger.trace("Saved jpmcPayment $payment")
            payment
        }
    }

    private fun Payment.asDynamoItem() = mapOf(
        DynamoDBJpmcAttribute.PK.param to (pkValuePrefix + this.paymentId).toAttributeValue(),
        DynamoDBJpmcAttribute.SK.param to this.paymentId.toAttributeValue(),
        DynamoDBJpmcAttribute.SOI.param to this.supplierOrderId.toAttributeValue(),
        DynamoDBJpmcAttribute.TX.param to this.paymentId.toAttributeValue(),
        DynamoDBJpmcAttribute.A.param to this.amount.toAttributeValue(),
        DynamoDBJpmcAttribute.PO.param to this.paymentOption?.toAttributeValue(),
        DynamoDBJpmcAttribute.RC.param to this.responseCode?.toAttributeValue(),
        DynamoDBJpmcAttribute.M.param to this.message?.toAttributeValue(),
        DynamoDBJpmcAttribute.ED.param to this.encData?.toAttributeValue()
    )
}

data class JpmcNotFound(val jpmcId: String) : RuntimeException("Cannot find any jpmc information for $jpmcId")
