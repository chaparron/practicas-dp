package adapters.repositories

import anySupplier
import adapters.infrastructure.CreateTableRequest
import adapters.infrastructure.DynamoDbContainer
import adapters.infrastructure.DynamoTestSupport
import adapters.infrastructure.DynamoTestSupport.supplierTable
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import randomString
import software.amazon.awssdk.services.dynamodb.DynamoDbClient

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class DynamoDBSupplierRepositoryTest {

    companion object {
        @JvmStatic
        @Container
        val container: DynamoDbContainer = DynamoDbContainer()
    }

    private lateinit var dynamoDbClient: DynamoDbClient

    private lateinit var sut: DynamoDBSupplierRepository

    @BeforeAll
    fun setUp() {
        dynamoDbClient = DynamoTestSupport.dynamoDbClient(container.endpoint())
        sut = DynamoDBSupplierRepository(dynamoDbClient, supplierTable)

        CreateTableRequest(
            tableName = supplierTable,
            attributes = listOf(CreateTableRequest.Param(DynamoDBAttribute.PK.param)),
            pk = DynamoDBAttribute.PK.param
        ).doExecuteWith(dynamoDbClient, DynamoTestSupport::createTable)
    }

    @Test
    fun `finds saved bank account by supplierId`() {
        val saved = sut.save(anySupplier())

        val retrieved = sut.get(saved.supplierId)

        assertEquals(saved, retrieved)
    }

    @Test
    fun `throws SupplierNotFound when bank account does not exist`() {
        val supplierId = randomString()

        assertFailsWith<SupplierNotFound> {
            sut.get(supplierId)
        }
    }
}
