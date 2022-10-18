package adapters.repositories.supplierorderdelay

import adapters.repositories.supplier.DynamoDBSupplierAttribute
import domain.model.SupplierOrderDelayEvent
import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import toAttributeValue

interface SupplierOrderDelayRepository {
    fun save(supplierOrderDelayEvent: SupplierOrderDelayEvent): SupplierOrderDelayEvent
    fun get(supplierId: Long): SupplierOrderDelayEvent
}

class DynamoDBOrderDelayRepository(
    private val dynamoDbClient: DynamoDbClient,
    private val tableName: String
) : SupplierOrderDelayRepository {
    companion object {
        private val logger = LoggerFactory.getLogger(SupplierOrderDelayRepository::class.java)
        private const val pkValuePrefix = "SupplierOrderDelay#"
    }

    override fun save(supplierOrderDelayEvent: SupplierOrderDelayEvent): SupplierOrderDelayEvent {
        return dynamoDbClient.putItem {
            logger.info("Saving supplier order delay: $supplierOrderDelayEvent")
            it.tableName(tableName).item(supplierOrderDelayEvent.asDynamoItem())
        }.let {
            logger.trace("Saved supplier order delay: $supplierOrderDelayEvent")
            supplierOrderDelayEvent
        }
    }

    override fun get(supplierId: Long): SupplierOrderDelayEvent =
        dynamoDbClient.getItem {
            it.tableName(tableName).key(supplierId.asGetItemKey())
        }.let { response ->
            response.takeIf { it.hasItem() }?.item()?.asSupplierOrderDelayEvent()
                ?: throw SupplierOrderDelayEventNotFound(supplierId)
        }

    private fun SupplierOrderDelayEvent.asDynamoItem() = mapOf(
        DynamoDBSupplierOrderDelayEventAttribute.PK.param to "$pkValuePrefix${this.supplierOrderId}".toAttributeValue(),
        DynamoDBSupplierOrderDelayEventAttribute.SK.param to this.supplierOrderId.toAttributeValue(),
        DynamoDBSupplierOrderDelayEventAttribute.OI.param to this.supplierOrderId.toAttributeValue(),
        DynamoDBSupplierOrderDelayEventAttribute.D.param to this.delay.toString().toAttributeValue(),
        DynamoDBSupplierOrderDelayEventAttribute.DT.param to this.delayTime.toString().toAttributeValue()
    )

    private fun Long.asGetItemKey() = mapOf(
        DynamoDBSupplierOrderDelayEventAttribute.PK.param to "$pkValuePrefix$this".toAttributeValue(),
        DynamoDBSupplierOrderDelayEventAttribute.SK.param to this.toAttributeValue()
    )

    private fun Map<String, AttributeValue>.asSupplierOrderDelayEvent() =
        SupplierOrderDelayEvent(
            supplierOrderId = this.getValue(DynamoDBSupplierAttribute.SI.param).s().toLong(),
            delay = this.getValue(DynamoDBSupplierOrderDelayEventAttribute.D.param).bool(),
            delayTime = this.getValue(DynamoDBSupplierOrderDelayEventAttribute.DT.param).hashCode(), //WTF Dynamo!!
        )
}

data class SupplierOrderDelayEventNotFound(val supplierId: Long) :
    RuntimeException("Cannot find any supplier order for $supplierId")
