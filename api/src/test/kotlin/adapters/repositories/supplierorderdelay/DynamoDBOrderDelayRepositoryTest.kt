package adapters.repositories.supplierorderdelay

import adapters.infrastructure.DynamoDbContainer
import domain.model.SupplierOrderDelay
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.verify
import org.testcontainers.containers.localstack.LocalStackContainer
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import wabipay.commons.dynamodb.testing.DynamoDbTestUtils
import java.net.URI

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class DynamoDBOrderDelayRepositoryTest {

    private val dynamoDbTestUtils = DynamoDbTestUtils( // WTF
        DynamoDbContainer.localStack.getEndpointConfiguration(LocalStackContainer.Service.DYNAMODB).serviceEndpoint
            .let { URI.create(it) }
    )
    private var dynamoDbClient: DynamoDbClient = dynamoDbTestUtils.dynamoDb

    private lateinit var sut: DynamoDBOrderDelayRepository

    private val anySupplierOrderDelay = SupplierOrderDelay(
        supplierOrderId = 77L,
        delay = true,
        delayTime = 60
    )

    @BeforeAll
    fun init() {
        sut = DynamoDBOrderDelayRepository(dynamoDbClient, DynamoDbTestUtils.tableName)
        dynamoDbTestUtils.createSingleTable(DynamoDbContainer.digitalPaymentTableSchema())
    }

    @BeforeEach
    fun setup() {
        dynamoDbTestUtils.removeAll()
    }

    @Test
    fun `should retrieve the same saved supplier order delay using the same id`() {
        // Given
        val supplierOrderDelayId = anySupplierOrderDelay.supplierOrderId
        // When
        val saved = sut.save(anySupplierOrderDelay)
        val retrieved = sut.get(supplierOrderDelayId)
        // Then
        assertEquals(saved, retrieved)
    }
}
