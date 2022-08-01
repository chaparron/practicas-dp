package adapters.repositories

import domain.model.Supplier
import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import toAttributeValue

interface SupplierRepository {
    fun save(supplier: Supplier): Supplier
    fun get(supplierId: String): Supplier
}

class DynamoDBSupplierRepository(
    private val dynamoDbClient: DynamoDbClient,
    private val tableName: String
) : SupplierRepository {

    companion object {
        private val logger = LoggerFactory.getLogger(DynamoDBSupplierRepository::class.java)
    }

    override fun save(supplier: Supplier): Supplier {
        return dynamoDbClient.putItem {
            logger.trace("Saving Supplier $supplier")
            it.tableName(tableName).item(supplier.asDynamoItem())
        }.let {
            logger.trace("Saved Supplier $supplier")
            supplier
        }
    }

    override fun get(supplierId: String): Supplier =
        dynamoDbClient.getItem {
            it.tableName(tableName).key(supplierId.asGetItemKey())
        }.let {  response ->
            response.takeIf { it.hasItem() }?.item()?.asSupplier() ?: throw SupplierNotFound(supplierId)
        }

    private fun Supplier.asDynamoItem() = mapOf(
        DynamoDBAttribute.PK.param to this.supplierId.toAttributeValue(),
        DynamoDBAttribute.SK.param to this.supplierId.toAttributeValue(),
        DynamoDBAttribute.SI.param to this.supplierId.toAttributeValue(),
        DynamoDBAttribute.S.param to this.state.toAttributeValue(),
        DynamoDBAttribute.N.param to this.bankAccountNumber.toAttributeValue(),
        DynamoDBAttribute.C.param to this.indianFinancialSystemCode.toAttributeValue()
    )

    private fun String.asGetItemKey() = mapOf(
        DynamoDBAttribute.PK.param to this.toAttributeValue(),
        DynamoDBAttribute.SK.param to this.toAttributeValue()
    )

    private fun Map<String, AttributeValue>.asSupplier() =
        Supplier(
            supplierId = this[DynamoDBAttribute.SI.param]?.s()!!,
            state = this[DynamoDBAttribute.S.param]?.s()!!,
            bankAccountNumber = this[DynamoDBAttribute.N.param]?.s()!!,
            indianFinancialSystemCode = this[DynamoDBAttribute.C.param]?.s()!!
        )
}

data class SupplierNotFound(val supplierId: String): RuntimeException("Cannot find any supplier for $supplierId")
