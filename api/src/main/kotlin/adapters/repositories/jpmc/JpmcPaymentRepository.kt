package adapters.repositories.jpmc

import domain.model.Payment
import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import toAttributeValue

interface JpmcPaymentRepository {

    fun findBy(paymentId: String): Payment
    fun save(payment: Payment): Payment
}

class DynamoDbJpmcPaymentRepository(
    private val dynamoDbClient: DynamoDbClient,
    private val tableName: String
) : JpmcPaymentRepository {

    companion object {
        private val logger = LoggerFactory.getLogger(DynamoDbJpmcPaymentRepository::class.java)
        private const val pkValuePrefix = "jpmc#"

        private val findByProjectionExpression: String = setOf(
            DynamoDBJpmcAttribute.SOI, DynamoDBJpmcAttribute.SK, DynamoDBJpmcAttribute.A, DynamoDBJpmcAttribute.PO, DynamoDBJpmcAttribute.RC, DynamoDBJpmcAttribute.M, DynamoDBJpmcAttribute.ED, DynamoDBJpmcAttribute.ST, DynamoDBJpmcAttribute.C, DynamoDBJpmcAttribute.LU
        ).joinToString(",") { it.param }
    }

    override fun findBy(paymentId: String): Payment {
        return dynamoDbClient.getItem {
            logger.trace("about to retrieve payment with id $paymentId")
            it.tableName(tableName).key(paymentId.keys()).projectionExpression(findByProjectionExpression)
        }.takeIf { it.hasItem() }?.item()?.let {
            Payment(
                supplierOrderId = it[DynamoDBJpmcAttribute.SOI]!!,
                paymentId = it[DynamoDBJpmcAttribute.SK]!!,
                amount = it[DynamoDBJpmcAttribute.A]!!,
                paymentOption = it[DynamoDBJpmcAttribute.PO],
                responseCode = it[DynamoDBJpmcAttribute.RC],
                message = it[DynamoDBJpmcAttribute.M],
                encData = it[DynamoDBJpmcAttribute.ED],
                status = enumValueOf(it[DynamoDBJpmcAttribute.ST]!!),
                createdAt = it[DynamoDBJpmcAttribute.C]!!,
                lastUpdatedAt = it[DynamoDBJpmcAttribute.LU]!!
            )
        } ?: throw PaymentNotFound(paymentId)
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

    private fun Payment.asDynamoItem() = this.paymentId.keys() + mapOf(
        DynamoDBJpmcAttribute.SOI.param to this.supplierOrderId.toAttributeValue(),
        DynamoDBJpmcAttribute.TX.param to this.paymentId.toAttributeValue(),
        DynamoDBJpmcAttribute.A.param to this.amount.toAttributeValue(),
        DynamoDBJpmcAttribute.PO.param to this.paymentOption?.toAttributeValue(),
        DynamoDBJpmcAttribute.RC.param to this.responseCode?.toAttributeValue(),
        DynamoDBJpmcAttribute.M.param to this.message?.toAttributeValue(),
        DynamoDBJpmcAttribute.ED.param to this.encData?.toAttributeValue(),
        DynamoDBJpmcAttribute.ST.param to this.status.name.toAttributeValue(),
        DynamoDBJpmcAttribute.C.param to this.createdAt.toAttributeValue(),
        DynamoDBJpmcAttribute.LU.param to this.lastUpdatedAt.toAttributeValue()
    )

    private val pk: pkValue = {
        (pkValuePrefix + this).toAttributeValue()
    }

    private val keys: keyValue = {
        mapOf(DynamoDBJpmcAttribute.PK.param to pk(), DynamoDBJpmcAttribute.SK.param to toAttributeValue())
    }

    private operator fun Map<String, AttributeValue>.get(key: DynamoDBJpmcAttribute): String? = this[key.param]?.s()

}

private typealias keyValue = String.() -> Map<String, AttributeValue>
private typealias pkValue = String.() -> AttributeValue

data class PaymentNotFound(val jpmcId: String) : RuntimeException("Cannot find any jpmc information for $jpmcId")
