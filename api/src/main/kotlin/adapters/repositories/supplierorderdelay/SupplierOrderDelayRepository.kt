package adapters.repositories.supplierorderdelay

import domain.model.SupplierOrderDelay
import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import toAttributeValue

interface SupplierOrderDelayRepository {
    fun save(supplierOrderDelay: SupplierOrderDelay): SupplierOrderDelay
    fun get(supplierId: Long): SupplierOrderDelay
}

class DynamoDBOrderDelayRepository(
    private val dynamoDbClient: DynamoDbClient,
    private val tableName: String
) : SupplierOrderDelayRepository {
    companion object {
        private val logger = LoggerFactory.getLogger(SupplierOrderDelayRepository::class.java)
        private const val pkValuePrefix = "SupplierOrderDelay#"
    }

    override fun save(supplierOrderDelay: SupplierOrderDelay): SupplierOrderDelay {
        return dynamoDbClient.putItem {
            logger.info("Saving supplier order delay: $supplierOrderDelay")
            it.tableName(tableName).item(supplierOrderDelay.asDynamoItem())
        }.let {
            logger.trace("Saved supplier order delay: $supplierOrderDelay")
            supplierOrderDelay
        }
    }

    override fun get(supplierId: Long): SupplierOrderDelay =
        dynamoDbClient.getItem {
            it.tableName(tableName).key(supplierId.asGetItemKey())
        }.let { response ->
            response.takeIf { it.hasItem() }?.item()?.asSupplierOrderDelay()
                ?: throw SupplierOrderDelayNotFound(supplierId)
        }

    private fun SupplierOrderDelay.asDynamoItem() = mapOf(
        DynamoDBSupplierOrderDelayAttribute.PK.param to "$pkValuePrefix${supplierOrderId}".toAttributeValue(),
        DynamoDBSupplierOrderDelayAttribute.SK.param to supplierOrderId.toAttributeValue(),
        DynamoDBSupplierOrderDelayAttribute.SOI.param to supplierOrderId.toAttributeValue(),
        DynamoDBSupplierOrderDelayAttribute.D.param to delay.toString().toAttributeValue(),
        DynamoDBSupplierOrderDelayAttribute.DT.param to delayTime.toString().toAttributeValue()
    )

    private fun Long.asGetItemKey() = mapOf(
        DynamoDBSupplierOrderDelayAttribute.PK.param to "$pkValuePrefix$this".toAttributeValue(),
        DynamoDBSupplierOrderDelayAttribute.SK.param to this.toAttributeValue()
    )

    private fun Map<String, AttributeValue>.asSupplierOrderDelay() =
        SupplierOrderDelay(
            supplierOrderId = this.getValue(DynamoDBSupplierOrderDelayAttribute.SOI.param).s().toLong(),
            delay = this.getValue(DynamoDBSupplierOrderDelayAttribute.D.param).s().toBoolean(),
            delayTime = this.getValue(DynamoDBSupplierOrderDelayAttribute.DT.param).s().toInt(),
        )
}

data class SupplierOrderDelayNotFound(val supplierId: Long) :
    RuntimeException("Cannot find any supplier order for $supplierId")
