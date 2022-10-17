package adapters.repositories.supplierorderdelay

import domain.model.SupplierOrderDelayEvent
import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import toAttributeValue

interface SupplierOrderDelayRepository {
    fun save(supplierOrderDelayEvent: SupplierOrderDelayEvent): SupplierOrderDelayEvent
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
        }.let{
            logger.trace("Saved supplier order delay: $supplierOrderDelayEvent")
            supplierOrderDelayEvent
        }
    }

    private fun SupplierOrderDelayEvent.asDynamoItem() = mapOf(
        DynamoDBSupplierOrderDelayEventAttribute.PK.param to "$pkValuePrefix${this.supplierOrderId}".toAttributeValue(),
        DynamoDBSupplierOrderDelayEventAttribute.SK.param to this.supplierOrderId.toAttributeValue(),
        DynamoDBSupplierOrderDelayEventAttribute.OI.param to this.supplierOrderId.toAttributeValue(),
        DynamoDBSupplierOrderDelayEventAttribute.D.param to this.delay.toString().toAttributeValue(),
        DynamoDBSupplierOrderDelayEventAttribute.DT.param to this.delayTime.toString().toAttributeValue()
    )
}
