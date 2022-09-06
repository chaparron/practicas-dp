package adapters.repositories.jpmc

import domain.model.Payment
import domain.model.PaymentForSave
import domain.model.PaymentForUpdate
import domain.model.errors.PaymentNotFound
import domain.model.errors.UpdatePaymentException
import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import toAttributeValue

interface JpmcPaymentRepository {

    fun findBy(paymentId: String): Payment
    fun save(payment: PaymentForSave): PaymentForSave

    fun update(payment: PaymentForUpdate)
}

class DynamoDbJpmcPaymentRepository(
    private val dynamoDbClient: DynamoDbClient,
    private val tableName: String
) : JpmcPaymentRepository {

    companion object {
        private val logger = LoggerFactory.getLogger(DynamoDbJpmcPaymentRepository::class.java)
        private const val pkValuePrefix = "jpmc#"

        private val findByProjectionExpression: String = setOf(
            DynamoDBJpmcAttribute.SOI, DynamoDBJpmcAttribute.SK, DynamoDBJpmcAttribute.A, DynamoDBJpmcAttribute.PO, DynamoDBJpmcAttribute.RC, DynamoDBJpmcAttribute.M, DynamoDBJpmcAttribute.ED, DynamoDBJpmcAttribute.ST, DynamoDBJpmcAttribute.C, DynamoDBJpmcAttribute.LU, DynamoDBJpmcAttribute.INV
        ).joinToString(",") { it.param }
    }

    override fun findBy(paymentId: String): Payment {
        return dynamoDbClient.getItem {
            logger.trace("about to retrieve payment with id $paymentId")
            it.tableName(tableName).key(paymentId.keys()).projectionExpression(findByProjectionExpression)
        }.takeIf { it.hasItem() }?.item()?.let {
            Payment(
                supplierOrderId = it[DynamoDBJpmcAttribute.SOI]!!.toLong(),
                paymentId = it[DynamoDBJpmcAttribute.SK]!!.toLong(),
                amount = it[DynamoDBJpmcAttribute.A]!!.toBigDecimal(),
                paymentOption = it[DynamoDBJpmcAttribute.PO],
                responseCode = it[DynamoDBJpmcAttribute.RC],
                message = it[DynamoDBJpmcAttribute.M],
                encData = it[DynamoDBJpmcAttribute.ED],
                status = enumValueOf(it[DynamoDBJpmcAttribute.ST]!!),
                createdAt = it[DynamoDBJpmcAttribute.C]!!,
                lastUpdatedAt = it[DynamoDBJpmcAttribute.LU]!!,
                invoiceId = it[DynamoDBJpmcAttribute.INV]
            )
        } ?: throw PaymentNotFound(paymentId)
    }

    override fun save(payment: PaymentForSave): PaymentForSave {
        return dynamoDbClient.putItem {
            logger.trace("Saving Jpmc payment $payment")
            it.tableName(tableName).item(payment.asDynamoItem())
        }.let {
            logger.trace("Saved jpmcPayment $payment")
            payment
        }
    }

    override fun update(payment: PaymentForUpdate) {
        runCatching {
            dynamoDbClient.updateItem {it
                .tableName(tableName)
                .key(payment.paymentId.toString().keys())
                .updateExpression(
                    "SET " +
                            "${DynamoDBJpmcAttribute.PO} = :paymentOption," +
                            "${DynamoDBJpmcAttribute.RC} = :responseCode," +
                            "${DynamoDBJpmcAttribute.M} = :message," +
                            "${DynamoDBJpmcAttribute.ED} = :encData," +
                            "${DynamoDBJpmcAttribute.ST} = :status," +
                            "${DynamoDBJpmcAttribute.LU} = :lastUpdated"
                )
                .expressionAttributeValues(mapOf(
                    ":paymentOption" to payment.paymentOption.toAttributeValue(),
                    ":responseCode" to payment.responseCode.toAttributeValue(),
                    ":message" to payment.message.toAttributeValue(),
                    ":encData" to payment.encData.toAttributeValue(),
                    ":status" to payment.status.name.toAttributeValue(),
                    ":lastUpdated" to payment.lastUpdatedAt.toAttributeValue()
                ))
                .conditionExpression("attribute_exists(${DynamoDBJpmcAttribute.PK})")
            }
        }.onFailure {
            logger.error("There was a problem updating the following payment: $payment.")
            throw UpdatePaymentException(payment)
        }.onSuccess {
            logger.info("Payment ${payment.paymentId} was successfully updated with ${payment.status} status.")
        }.getOrThrow()
    }

    private fun PaymentForSave.asDynamoItem() = this.paymentId.toString().keys() + mapOf(
        DynamoDBJpmcAttribute.SOI.param to this.supplierOrderId.toString().toAttributeValue(),
        DynamoDBJpmcAttribute.TX.param to this.paymentId.toString().toAttributeValue(),
        DynamoDBJpmcAttribute.A.param to this.amount.toString().toAttributeValue(),
        DynamoDBJpmcAttribute.ST.param to this.status.name.toAttributeValue(),
        DynamoDBJpmcAttribute.C.param to this.createdAt.toAttributeValue(),
        DynamoDBJpmcAttribute.LU.param to this.lastUpdatedAt.toAttributeValue(),
        DynamoDBJpmcAttribute.INV.param to this.invoiceId.toAttributeValue()
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

