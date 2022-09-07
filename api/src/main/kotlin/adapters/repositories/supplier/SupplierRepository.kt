package adapters.repositories.supplier

import domain.model.Supplier
import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import toAttributeValue

interface SupplierRepository {
    fun save(supplier: Supplier): Supplier
    fun get(supplierId: Long): Supplier
}

class DynamoDBSupplierRepository(
    private val dynamoDbClient: DynamoDbClient,
    private val tableName: String
) : SupplierRepository {

    companion object {
        private val logger = LoggerFactory.getLogger(DynamoDBSupplierRepository::class.java)
        private const val pkValuePrefix = "supplier#"
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

    override fun get(supplierId: Long): Supplier =
        dynamoDbClient.getItem {
            it.tableName(tableName).key(supplierId.asGetItemKey())
        }.let {  response ->
            response.takeIf { it.hasItem() }?.item()?.asSupplier() ?: throw SupplierNotFound(supplierId)
        }

    private fun Supplier.asDynamoItem() = mapOf(
        DynamoDBSupplierAttribute.PK.param to "$pkValuePrefix${this.supplierId}".toAttributeValue(),
        DynamoDBSupplierAttribute.SK.param to this.supplierId.toAttributeValue(),
        DynamoDBSupplierAttribute.SI.param to this.supplierId.toAttributeValue(),
        DynamoDBSupplierAttribute.N.param to this.bankAccountNumber.toAttributeValue(),
        DynamoDBSupplierAttribute.C.param to this.indianFinancialSystemCode.toAttributeValue()
    )

    private fun Long.asGetItemKey() = mapOf(
        DynamoDBSupplierAttribute.PK.param to "$pkValuePrefix$this".toAttributeValue(),
        DynamoDBSupplierAttribute.SK.param to this.toAttributeValue()
    )

    private fun Map<String, AttributeValue>.asSupplier() =
        Supplier(
            supplierId = this.getValue(DynamoDBSupplierAttribute.SI.param).s().toLong(),
            bankAccountNumber = this.getValue(DynamoDBSupplierAttribute.N.param).s(),
            indianFinancialSystemCode = this.getValue(DynamoDBSupplierAttribute.C.param).s()
        )
}

data class SupplierNotFound(val supplierId: Long): RuntimeException("Cannot find any supplier for $supplierId")
