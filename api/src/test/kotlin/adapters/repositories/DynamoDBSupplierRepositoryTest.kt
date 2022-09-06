package adapters.repositories

import adapters.infrastructure.DynamoDbContainer
import adapters.infrastructure.DynamoDbContainer.digitalPaymentTableSchema
import adapters.repositories.supplier.DynamoDBSupplierRepository
import adapters.repositories.supplier.SupplierNotFound
import anySupplier
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.localstack.LocalStackContainer.Service.DYNAMODB
import randomLong
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import wabipay.commons.dynamodb.testing.DynamoDbTestUtils
import java.net.URI
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class DynamoDBSupplierRepositoryTest {
    private val dynamoDbTestUtils = DynamoDbTestUtils(
        DynamoDbContainer.localStack.getEndpointConfiguration(DYNAMODB).serviceEndpoint
            .let { URI.create(it) }
    )
    private var dynamoDbClient: DynamoDbClient = dynamoDbTestUtils.dynamoDb

    private lateinit var sut: DynamoDBSupplierRepository

    @BeforeAll
    fun init() {
        sut = DynamoDBSupplierRepository(dynamoDbClient, DynamoDbTestUtils.tableName)
        dynamoDbTestUtils.createSingleTable(digitalPaymentTableSchema())
    }

    @BeforeEach
    fun setup(){
        dynamoDbTestUtils.removeAll()
    }

    @Test
    fun `finds saved bank account by supplierId`() {
        val saved = sut.save(anySupplier())

        val retrieved = sut.get(saved.supplierId)

        assertEquals(saved, retrieved)
    }

    @Test
    fun `throws SupplierNotFound when bank account does not exist`() {
        val supplierId = randomLong()

        assertFailsWith<SupplierNotFound> {
            sut.get(supplierId)
        }
    }
}
