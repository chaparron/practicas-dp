package adapters.repositories.jpmc

import domain.model.JpmcPayment
import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import toAttributeValue

interface JpmcPaymentRepository {
    fun save(jpmcPayment: JpmcPayment): JpmcPayment
}

class DynamoDbJpmcPaymentRepository(
    private val dynamoDbClient: DynamoDbClient,
    private val tableName: String
) : JpmcPaymentRepository {

    companion object {
        private val logger = LoggerFactory.getLogger(DynamoDbJpmcPaymentRepository::class.java)
        private const val pkValuePrefix = "jpmc#"
    }

    override fun save(jpmcPayment: JpmcPayment): JpmcPayment {
        return dynamoDbClient.putItem {
            logger.trace("Saving Jpmc payment $jpmcPayment")
            it.tableName(tableName).item(jpmcPayment.asDynamoItem())
        }.let {
            logger.trace("Saved jpmcPayment $jpmcPayment")
            jpmcPayment
        }
    }

    private fun JpmcPayment.asDynamoItem() = mapOf(
        DynamoDBJpmcAttribute.PK.param to (pkValuePrefix + this.txnRefNo).toAttributeValue(),
        DynamoDBJpmcAttribute.SK.param to this.txnRefNo.toAttributeValue(),
        DynamoDBJpmcAttribute.SOI.param to this.supplierOrderId.toAttributeValue(),
        DynamoDBJpmcAttribute.TX.param to this.txnRefNo.toAttributeValue(),
        DynamoDBJpmcAttribute.A.param to this.amount.toAttributeValue(),
        DynamoDBJpmcAttribute.TA.param to this.totalAmount.toAttributeValue(),
        DynamoDBJpmcAttribute.PO.param to this.paymentOption?.toAttributeValue(),
        DynamoDBJpmcAttribute.RC.param to this.responseCode?.toAttributeValue(),
        DynamoDBJpmcAttribute.M.param to this.message?.toAttributeValue(),
        DynamoDBJpmcAttribute.ED.param to this.encData?.toAttributeValue()
    )
}

data class JpmcNotFound(val jpmcId: String) : RuntimeException("Cannot find any jpmc information for $jpmcId")
